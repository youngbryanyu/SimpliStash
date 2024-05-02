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
 * The SET command. Sets a key's value in the default stash.
 */
@Component
public class SetCommand implements WriteCommand {
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
     * Responds with OK.
     * 
     * Format: SET <key> <value>
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Return null if not enough commands */
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
        String value = tokens.pollFirst();

        /* Return error if client is in read-only mode */
        if (readOnly) {
            logger.debug(String.format("SET %s --> %s (failed, read-only mode)", key, value));
            return ProtocolUtil.buildErrorResponse("Cannot SET in read-only mode");
        }

        /* Return error if key is too big */
        if (key.length() > Stash.MAX_KEY_SIZE) {
            logger.debug(String.format("SET %s --> %s (failed, key is too big)", key, value));
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        /* Return error if value is too big */
        if (value.length() > Stash.MAX_VALUE_SIZE) {
            logger.debug(String.format("SET %s --> %s (failed, value is too big)", key, value));
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        /* Set a new value */
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        stash.set(key, value);

        /* Return OK */
        logger.debug(String.format("SET %s --> %s", key, value));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     * 
     * @return The command name
     */
    public String getName() {
        return NAME;
    }
}
