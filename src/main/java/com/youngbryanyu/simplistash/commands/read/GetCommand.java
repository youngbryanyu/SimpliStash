package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The GET command. Gets a key's value from the default stash.
 */
@Component
public class GetCommand implements ReadCommand {
    /**
     * The command's name
     */
    private static final String NAME = "GET";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "GET <key> <num_optional_args> [NAME]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The name of the optional name arg.
     */
    private static final String ARG_NAME = "NAME";
    /**
     * The stash manager.
     */
    private final StashManager stashManager;
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the GET command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public GetCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the GET command. 
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens) {
        /* Return null if not enough arguments */
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
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);

        /**
         * Return error if num optional args is malformed.
         */
        if (numOptionalArgs == -1) {
            logger.debug(String.format("GET %s (failed, invalid optional args count)", key));
            return ProtocolUtil.buildErrorResponse("GET failed, invalid optional args count");
        }

        /* Re-add tokens and return null if not enough args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);

        /* Return error message if error occurred while processing optional args */
        if (optionalArgVals == null) {
            logger.debug(String.format("GET %s (failed, invalid optional args)", key));
            return ProtocolUtil.buildErrorResponse("GET failed, invalid optional args");
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
            logger.debug(String.format("GET {%s} %s (failed, stash doesn't exist)", name, key));
            return ProtocolUtil.buildErrorResponse("GET failed, stash doesn't exist.");
        }

         /* Get the value */
        String value = stash.get(key);

        /* Return the value, or the null string if null */
        logger.debug(String.format("GET %s", key, value));
        return (value == null)
                ? ProtocolUtil.buildNullResponse()
                : ProtocolUtil.buildValueResponse(value);
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
