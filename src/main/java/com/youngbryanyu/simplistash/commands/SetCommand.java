package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SET command. Sets a key's value in the default stash.
 */
@Component
public class SetCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "SET";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "SET <key> <value>";
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
     * Constructor for the SET command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public SetCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the SET command. Stores the key value pair in the default stash.
     * Returns null if there aren't enough tokens. Returns an error response if the
     * key or value length are too big. Responds with OK.
     * 
     * Format: SET <key> <value>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst();  /* Remove command token */
        
        String key = tokens.pollFirst();
        if (key.length() > Stash.MAX_KEY_SIZE) {
            logger.debug(String.format("SET %s --> * (failed, key is too big)", key));
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        String value = tokens.pollFirst();
        if (value.length() > Stash.MAX_VALUE_SIZE) {
            logger.debug(String.format("SET %s --> %s (failed, value is too big)", key));
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        String response = stash.set(key, value); /* Set a new value */

        logger.debug(String.format("SET %s --> %s", key, value));
        return response;
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
