package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SET command.
 */
@Component
public class SetCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "SET";
    /**
     * The stash manager.
     */
    private StashManager stashManager;
    /**
     * The application logger.
     */
    private Logger logger;

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
     * Returns null if there aren't enough tokens. Returns an error response if the
     * key or value length are too big. Responds with OK.
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < 3) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */
        
        String key = tokens.pollFirst();
        if (key.length() > Stash.MAX_KEY_SIZE) {
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        String value = tokens.pollFirst();
        if (value.length() > Stash.MAX_VALUE_SIZE) {
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        stash.set(key, value);

        logger.info(String.format("Command: SET %s --> %s", key, value));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
