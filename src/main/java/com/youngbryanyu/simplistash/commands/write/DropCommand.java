package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The DROP command. Deletes an entire stash.
 */
@Component
public class DropCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "DROP";
    /**
     * The base format of the command
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
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the DROP command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public DropCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the DROP command. Deletes a stash. Responds with OK.
     * 
     * Format: DROP <name>
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Return null if not enough arguments */
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
            logger.debug(String.format("DROP %s (failed, read-only mode)", name));
            return ProtocolUtil.buildErrorResponse("Cannot DROP in read-only mode");
        }

        /* Return error if attempting to drop the default stash */
        if (name.equals(StashManager.DEFAULT_STASH_NAME)) {
            logger.debug("DROP %s (failed, cannot drop default stash)");
            return ProtocolUtil.buildErrorResponse("Cannot drop the default stash.");
        }

        /* Drop the stash */
        stashManager.dropStash(name); 

        /* Return OK */
        logger.debug(String.format("DROP %s", name));
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
