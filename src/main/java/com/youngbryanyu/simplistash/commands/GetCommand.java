package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

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
public class GetCommand implements Command{
    /**
     * The command's name
     */
    private static final String NAME = "GET";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "GET <key>";
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
     * Constructor for the GET command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public GetCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the GET command. Returns null if there aren't enough tokens.
     * Responds with the value if the key exists, or the encoded null string if the
     * key doesn't exist.
     * 
     * Format: GET <key>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */
        
        String key = tokens.pollFirst();
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        String value = stash.get(key); /* Get the value */

        logger.debug(String.format("GET %s", key, value));
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
