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
     * Constructor for the SSET command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public SSetCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
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
        String key = tokens.pollFirst();
        String value = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);

        /**
         * Return error if num optional args is malformed.
         */
        if (numOptionalArgs == -1) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, invalid optional args count)", name, key, value));
            return ProtocolUtil.buildErrorResponse("SSET failed, invalid optional args count");
        }

        /* Re-add tokens and return null if not enough args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(value);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Return error if client is in read-only mode */
        if (readOnly) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, read-only mode)", name, key, value));
            return ProtocolUtil.buildErrorResponse("Cannot SSET in read-only mode");
        }

        /* Return error if key is too big */
        if (key.length() > Stash.MAX_KEY_SIZE) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, key is too big)", name, key, value));
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        /* Return error if value is too big */
        if (value.length() > Stash.MAX_VALUE_SIZE) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, value is too big)", name, key, value));
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);

        /* Return error message if error occurred while processing optional args */
        if (optionalArgVals == null) {
            logger.debug(String.format("SSET {%s} %s --> %s (failed, invalid optional args)", name, key, value));
            return ProtocolUtil.buildErrorResponse("SSET failed, invalid optional args");
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

         /* Return an error if the stash doesn't exist. */
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            logger.debug(String.format("SSET {%s} %s --> % (failed, stash doesn't exist)", name, key, value));
            return ProtocolUtil.buildErrorResponse("SSET failed, stash doesn't exist.");
        }

        /* Set a new value */
        if (ttl == -1) {
            stash.set(key, value);
        } else {
            stash.setWithTTL(key, value, ttl); /* Set with TTL if specified */
        }

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

// TODO: copy logic from SET for TTL