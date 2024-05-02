package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SGET command. Gets a key's value from the specified stash.
 */
@Component
public class SGetCommand implements ReadCommand {
    /**
     * The command's name
     */
    private static final String NAME = "SGET";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "SGET <name> <key>";
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
     * Constructor for the SGET command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public SGetCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the SGET command. Responds with the value corresponding to the key
     * in the specified stash if the key exists, or the encoded null string if the
     * key doesn't exist.
     * 
     * Format: SGET <name> <key>
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens) {
        /* Return null if not enough tokens */
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

        /* Get the stash. Return an error if the stash doesn't exist. */
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            logger.debug(String.format("SGET {%s} %s (failed, stash doesn't exist)", name, key));
            return ProtocolUtil.buildErrorResponse("SGET failed, stash doesn't exist.");
        }

        /**
         * Get the value corresponding to the key. In the edge case that the stash's DB
         * is being closed concurrently or is already closed, stash.get() will catch the
         * exceptions/errors.
         */
        String value = stash.get(key);

        /* Return the value, or the null string if null */
        logger.debug(String.format("SGET {%s} %s", name, key));
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
