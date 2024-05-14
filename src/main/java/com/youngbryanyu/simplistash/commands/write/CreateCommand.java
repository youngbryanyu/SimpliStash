package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The CREATE command. Creates a new stash.
 */
@Component
public class CreateCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "CREATE";
    /**
     * The command's format.
     */
    private static final String FORMAT = "CREATE <name>";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The stash manager.
     */
    private final StashManager stashManager;

    /**
     * Constructor for the CREATE command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public CreateCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = ProtocolUtil.getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the CREATE command. Returns null if there aren't enough tokens.
     * 
     * @param tokens   The client's tokens.
     * @param readOnly Whether the client is read-only.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Check if there are enough tokens */
        if (tokens.size() < minRequiredArgs) {
            return null;
        }

        /* Extract tokens */
        tokens.pollFirst();
        String name = tokens.pollFirst();

        /* Check if client is read-only */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.READ_ONLY_MODE));
        }

        /* Validate stash name */
        if (name.length() > Stash.MAX_NAME_LENGTH) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_NAME_TOO_LONG));
        } else if (stashManager.containsStash(name)) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_NAME_TAKEN));
        }

        /* Create stash */
        boolean createdSuccessfully = stashManager.createStash(name);
        if (!createdSuccessfully) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_LIMIT_REACHED));
        }

        /* Build response */
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     * 
     * @return THe command's name.
     */
    public String getName() {
        return NAME;
    }
}
