package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The DELETE command.
 */
@Component
public class DeleteCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "DELETE";
    /**
     * The stash manager.
     */
    private StashManager stashManager;
    /**
     * The application logger.
     */
    private Logger logger;

    /**
     * Constructor for the DELETE command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public DeleteCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the DELETE command. Returns null if there aren't enough tokens. Responds with OK.
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < 2) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */
        String key = tokens.pollFirst();
        Stash stash = stashManager.getStash(StashManager.DEFAULT_STASH_NAME);
        stash.delete(key);

        logger.info(String.format("Command: DELETE %s", key));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
