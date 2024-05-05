package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The DROP command. Drops a stash.
 */
@Component
public class DropCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "DROP";
    /**
     * The command's format.
     */
    private static final String FORMAT = "DROP <name>";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The stash manager.
     */
    private final StashManager stashManager;

    /**
     * Constructor for the DROP command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public DropCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the DROP command. Returns null if there aren't enough tokens.
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
            return ProtocolUtil.buildErrorResponse("DROP failed, read-only mode");
        }

        /* Check if attempting to drop default stash */
        if (name.equals(StashManager.DEFAULT_STASH_NAME)) {
            return ProtocolUtil.buildErrorResponse("DROP failed, cannot drop the default stash.");
        }

        /* Drop stash */
        stashManager.dropStash(name);

        /* Build response */
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     * 
     * @return The command's name.
     */
    public String getName() {
        return NAME;
    }
}
