package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;

import org.slf4j.Logger;
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
    private static final String NAME = "CREATE";
    /**
     * The base format of the command
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
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the CREATE command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public CreateCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the CREATE command. Creates a new stash. 
     * 
     * Format: CREATE <name>
     * 
     * @param tokens   The client's tokens.
     * @param readOnly Whether the client is read-only.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Return null if not enough args */
        if (tokens.size() < minRequiredArgs) {
            return null;
        }

        /*
         * Remove all tokens associated with the command. This should be done at the
         * start in order to not pollute future command execution in case the command
         * exits early due to an error.
         */
        tokens.pollFirst();
        String name = tokens.pollFirst();

        /* Return error if client is in read-only mode */
        if (readOnly) {
            logger.debug(String.format("CREATE %s (failed, read-only mode)", name));
            return ProtocolUtil.buildErrorResponse("Cannot CREATE in read-only mode");
        }
        
        /* Return error if stash name is too long */
        if (name.length() > Stash.MAX_NAME_SIZE) {
            logger.debug(String.format("CREATE %s (failed, name exceeds size limit)", name));
            return ProtocolUtil.buildErrorResponse("The name exceeds the size limit.");
        }

        /* Return error if stash name is already taken */
        if (stashManager.containsStash(name)) {
            logger.debug(String.format("CREATE %s (failed, name already taken)", name));
            return ProtocolUtil.buildErrorResponse("The stash name is already taken.");
        }

        /* Create the stash */
        boolean createdSuccessfully = stashManager.createStash(name); 

        /* Return error if couldn't create stash (too many existing already) */
        if (!createdSuccessfully) {
            logger.debug(String.format("CREATE %s (failed, max number of stashes reached)", name));
            return ProtocolUtil.buildErrorResponse("The max number of stashes has been reached.");
        }

        /* Return OK response */
        logger.debug(String.format("CREATE %s", name));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command name.
     * 
     * @return THe command's name.
     */
    public String getName() {
        return NAME;
    }
}
