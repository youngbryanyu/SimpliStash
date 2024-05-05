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
    private static final String FORMAT = "GET <key>";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs; 
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
     * Executes the GET command. Responds with the value corresponding to the key
     * in the default stash if the key exists, or the encoded null string if the
     * key doesn't exist.
     * 
     * Format: GET <key>
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

        /* Get the value */
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
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
