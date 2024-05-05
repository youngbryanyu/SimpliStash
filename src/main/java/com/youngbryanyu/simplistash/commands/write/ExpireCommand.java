package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The EXPIRE command. Sets a TTL for a key.
 */
@Component
public class ExpireCommand implements WriteCommand {
    /**
     * The command's name.
     */
    private static final String NAME = "EXPIRE";
    /**
     * The base format of the command.
     */
    private static final String FORMAT = "EXPIRE <key> <TTL> <num_optional_args> [NAME]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The name of the optional name arg.
     */
    private static final String ARG_NAME = "NAME";
    /**
     * The name of the optional ttl arg.
     */
    private static final String ARG_TTL = "TTL";
    /**
     * The stash manager.
     */
    private final StashManager stashManager;
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the EXPIRE command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public ExpireCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the EXPIRE command.
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
        String ttlStr = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);

        /**
         * Return error if num optional args is malformed.
         */
        if (numOptionalArgs == -1) {
            logger.debug(String.format("EXPIRE %s --> %s (failed, invalid optional args count)", key, ttlStr));
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, invalid optional args count");
        }

        /* Re-add tokens and return null if not enough args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(ttlStr);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Return error after extracting tokens if client is in read-only mode */
        if (readOnly) {
            logger.debug(String.format("EXPIRE %s --> %s (failed, read-only mode)", key, ttlStr));
            return ProtocolUtil.buildErrorResponse("Cannot EXPIRE in read-only mode");
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);

        /* Return error message if error occurred while processing optional args */
        if (optionalArgVals == null) {
            logger.debug(String.format("EXPIRE %s --> %s (failed, invalid optional args)", key, ttlStr));
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, invalid optional args");
        }

        /* Get the stash name */
        String name;
        if (optionalArgVals.containsKey(ARG_NAME)) {
            name = optionalArgVals.get(ARG_NAME);
        } else {
            name = StashManager.DEFAULT_STASH_NAME;
        }

        /* Get the stash, check if it exists */
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            logger.debug(String.format("EXPIRE {%s} %s (failed, stash doesn't exist)", name, key));
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, stash doesn't exist.");
        }

        /* Get the TTL */
        long ttl;
        try {
            ttl = Long.parseLong(ttlStr);
        } catch (NumberFormatException e) {
            return ProtocolUtil.buildErrorResponse("TTL must be a valid long.");
        }
        if (ttl <= 0 || ttl > Command.MAX_TTL) {
            return ProtocolUtil.buildErrorResponse("The TTL value must be in the range [1, 157_784_630_000]");
        }

        /* Check if the key exists */
        if (!stash.contains(key)) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, the key doesn't exist in the stash.");
        }

        /* Set a new value */
        stash.updateTTL(key, ttl);

        /* Return OK */
        logger.debug(String.format("EXPIRE %s --> %s", key, ttl));
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
