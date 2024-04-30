package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The DELETE command. Deletes a key from the default stash.
 */
@Component
public class DeleteCommand implements Command {
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
     * Executes the DELETE command. Returns null if there aren't enough tokens. Responds with OK.
     * 
     * Format: DELETE <key>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */

        String key = tokens.pollFirst();
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        String response = stash.delete(key);

        logger.debug(String.format("DELETE %s", key));
        return response;
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
