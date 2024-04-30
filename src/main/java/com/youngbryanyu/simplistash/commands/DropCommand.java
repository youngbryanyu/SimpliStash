package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private static final int MIN_REQUIRED_ARGS = Command.getMinRequiredArgs(FORMAT);
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
    }

    /**
     * Executes the DROP command. Deletes a stash. Returns null if there aren't
     * enough tokens. Returns an error response if attempting to drop the "default"
     * stash. Responds with OK.
     * 
     * Format: DROP <name>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */

        String name = tokens.pollFirst();
        if (name.equals(StashManager.DEFAULT_STASH_NAME)) {
            logger.debug("DROP %s (failed, cannot drop default stash)");
            return ProtocolUtil.buildErrorResponse("Cannot drop the default stash.");
        }

        stashManager.dropStash(name);

        logger.debug(String.format("DROP %s", name));
        return ProtocolUtil.buildOkResponse();
    }

    public String getName() {
        return NAME;
    }
}
