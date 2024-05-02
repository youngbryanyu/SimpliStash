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
 * The DELETE command. Deletes a key from the default stash.
 */
@Component
public class DeleteCommand implements WriteCommand {
    /**
     * The command's name.
     */
    private static final String NAME = "DELETE";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "DELETE <key>";
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
     * Constructor for the DELETE command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public DeleteCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the DELETE command. Returns null if there aren't enough tokens.
     * Responds with OK.
     * 
     * Format: DELETE <key>
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Return null if not enough args */
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        /*
         * Remove all tokens associated with the command. This should be done at the
         * start in order to not pollute future command execution in case the command
         * exits early due to an error.
         */
        tokens.pollFirst();
        String key = tokens.pollFirst();

        /* Return error if client is in read-only mode */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse("Cannot CREATE in read-only mode");
        }

        /* Delete the key */
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        stash.delete(key);

        /* Return OK */
        logger.debug(String.format("DELETE %s", key));
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
