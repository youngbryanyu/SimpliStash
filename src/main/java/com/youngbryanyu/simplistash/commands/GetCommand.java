package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The GET command.
 */
@Component
public class GetCommand implements Command{
    /**
     * The command's name
     */
    public static final String NAME = "GET";
    /**
     * The stash manager.
     */
    private StashManager stashManager;
    /**
     * The application logger.
     */
    private Logger logger;

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
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < 2) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */
        String key = tokens.pollFirst();
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        String value = stash.get(key);

        logger.info(String.format("Command: GET %s", key, value));
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
