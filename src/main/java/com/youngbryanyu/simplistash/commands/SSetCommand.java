package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SSET command. Sets a key's value in the specified stash.
 */
@Component
public class SSetCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "SSET";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "SSET <name> <key> <value>";
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
     * Constructor for the SSET command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public SSetCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the SSET command. Stores the key value pair in the specified stash.
     * Returns null if there aren't enough tokens. Returns an error response if the
     * key or value length are too big, or if the stash doesn't exist. Responds with
     * OK.
     * 
     * Format: SSET <name> <key> <value>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */

        String name = tokens.pollFirst();
        Stash stash = stashManager.getStash(name);

        /**
         * We need to make this null check since another client may have concurrently
         * dropped the stash, causing stashManager.getStash() to return null.
         */
        if (stash == null) {
            logger.debug(String.format("SSET {%s} * --> * (failed, stash doesn't exist)", name));
            return ProtocolUtil.buildErrorResponse("SSET failed, stash doesn't exist.");
        }

        String key = tokens.pollFirst();
        if (key.length() > Stash.MAX_KEY_SIZE) {
            logger.debug(String.format("SSET {%s} %s --> * (failed, key is too big)", name, key));
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        String value = tokens.pollFirst();
        if (value.length() > Stash.MAX_VALUE_SIZE) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, value is too big)", name, key, value));
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        /**
         * In the edge case that the stash's DB is being closed concurrently or is
         * already closed, stash.set() will catch the exceptions/errors.
         */
        String response = stash.set(key, value); /* Set a new value */

        logger.debug(String.format("SSET {%s} %s --> %s", name, key, value));
        return response;
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
