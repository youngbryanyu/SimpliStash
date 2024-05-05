package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

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
     * The base format of the command.
     */
    private static final String FORMAT = "SET <key> <value> <num_optional_args> [TTL]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The name of the optional ttl arg.
     */
    private static final String ARG_NAME_TTL = "TTL";
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
        minRequiredArgs = getMinRequiredArgs(FORMAT);
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
        String key = tokens.pollFirst();
        String value = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);

        /**
         * Return error if num optional args is malformed.
         */
        if (numOptionalArgs == -1) {
            logger.debug(String.format("SET %s --> %s (failed, invalid optional args count)", key, value));
            return ProtocolUtil.buildErrorResponse("SET failed, invalid optional args count");
        }

        /* Re-add tokens and return null if not enough args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(value);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Return error after extracting tokens if client is in read-only mode */
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

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);

        /* Return error message if error occurred while processing optional args */
        if (optionalArgVals == null) {
            logger.debug(String.format("SET %s --> %s (failed, invalid optional args)", key, value));
            return ProtocolUtil.buildErrorResponse("SET failed, invalid optional args");
        }

        /* Set the TTL if specified */
        long ttl = -1;
        if (optionalArgVals.containsKey(ARG_NAME_TTL)) {
            try {
                ttl = Long.parseLong(optionalArgVals.get(ARG_NAME_TTL));
            } catch (NumberFormatException e) {
                return ProtocolUtil.buildErrorResponse("TTL must be a valid long.");
            }

            if (ttl <= 0 || ttl > Command.MAX_TTL) {
                ProtocolUtil.buildErrorResponse("The TTL value must be in the range [1, 157,784,630,000]");
            }
        }

        /* Set a new value */
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        if (ttl == -1) {
            stash.set(key, value);
        } else {
            stash.setWithTTL(key, value, ttl); /* Set with TTL if specified */
        }

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
