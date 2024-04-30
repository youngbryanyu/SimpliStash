package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SDELETE command. Deletes a key from the default stash.
 */
@Component
public class SDeleteCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "SDELETE";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "SDELETE <name> <key> <value>";
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
     * Constructor for the SDELETE command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public SDeleteCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the SDELETE command. Returns null if there aren't enough tokens.
     * Returns an error message if the specified stash doesn't exist. Responds with
     * OK.
     * 
     * Format: SDELETE <name> <key>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */

        String name = tokens.pollFirst();
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            logger.debug(String.format("SDELETE {%s} * (failed, stash doesn't exist)", name));
            return ProtocolUtil.buildErrorResponse("SDELETE failed, stash doesn't exist.");
        }

        String key = tokens.pollFirst();
       
        String response = stash.delete(key);

        logger.debug(String.format("SDELETE %s", key));
        return response;
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
