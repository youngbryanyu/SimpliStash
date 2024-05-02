package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The SDELETE command. Deletes a key from the default stash.
 */
@Component
public class SDeleteCommand implements WriteCommand {
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
     * Executes the SDELETE command. Responds with OK.
     * 
     * Format: SDELETE <name> <key>
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Return null if not enough arguments */
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

        /* Return error if client is in read-only mode */
        if (readOnly) {
            logger.debug(String.format("SDELETE {%s} %s (failed, read-only mode)", name, key));
            return ProtocolUtil.buildErrorResponse("Cannot SDELETE in read-only mode");
        }

        /* Get the stash. Return an error if the stash doesn't exist. */
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            logger.debug(String.format("SDELETE {%s} * (failed, stash doesn't exist)", name, key));
            return ProtocolUtil.buildErrorResponse("SDELETE failed, stash doesn't exist.");
        }

        /* Delete the key */
        stash.delete(key);

        /* Return OK */
        logger.debug(String.format("SDELETE %s", key));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     * 
     * @return The command name.
     */
    public String getName() {
        return NAME;
    }
}
