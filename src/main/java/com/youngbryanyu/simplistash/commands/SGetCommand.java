package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SGET command. Gets a key's value from the specified stash.
 */
@Component
public class SGetCommand implements Command {
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
     * Executes the SGET command. Returns null if there aren't enough tokens.
     * Responds with the value if the key exists, or the encoded null string if the
     * key doesn't exist. Returns an error message if the specified stash doesn't
     * exist.
     * 
     * Format: SGET <name> <key>
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
            logger.debug(String.format("SGET {%s} * (failed, stash doesn't exist)", name));
            return ProtocolUtil.buildErrorResponse("SGET failed, stash doesn't exist.");
        }

        // HTreeMap<String, String> map = 
        String key = tokens.pollFirst();

        /**
         * In the edge case that the stash's DB is being closed concurrently or is
         * already closed, stash.get() will catch the exceptions/errors.
         */
        String value = stash.get(key); /* Get the value */

        logger.debug(String.format("SGET {%s} %s", name, key));
        return (value == null)
                ? ProtocolUtil.buildNullResponse()
                : ProtocolUtil.buildValueResponse(value);
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
