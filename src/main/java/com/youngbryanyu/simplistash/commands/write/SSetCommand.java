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
 * The SSET command. Sets a key's value in the specified stash.
 */
@Component
public class SSetCommand implements WriteCommand {
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
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        /*
         * Remove all tokens associated with the command. This should be done at the
         * start in order to not pollute future command execution in case the command
         * exits early due to an error.
         */
        tokens.pollFirst();
        String name = tokens.pollFirst();
        String key = tokens.pollFirst();
        String value = tokens.pollFirst();

        /* Return error if client is in read-only mode */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse("Cannot SSET in read-only mode");
        }

        /* Return error if key is too big */
        if (key.length() > Stash.MAX_KEY_SIZE) {
            logger.debug(String.format("SSET {%s} %s --> * (failed, key is too big)", name, key));
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        /* Return error if value is too big */
        if (value.length() > Stash.MAX_VALUE_SIZE) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, value is too big)", name, key, value));
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        /* Get the stash. Return an error if the stash doesn't exist. */
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            logger.debug(String.format("SSET {%s} * (failed, stash doesn't exist)", name));
            return ProtocolUtil.buildErrorResponse("SSET failed, stash doesn't exist.");
        }

        /* Set a new value */
        stash.set(key, value);

        /* Return OK */
        logger.debug(String.format("SSET {%s} %s --> %s", name, key, value));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
