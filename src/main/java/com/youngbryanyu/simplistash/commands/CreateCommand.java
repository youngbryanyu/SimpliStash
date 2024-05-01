package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The CREATE command. Creates a new stash.
 */
@Component
public class CreateCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "CREATE";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "CREATE <name>";
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
     * Constructor for the CREATE command.
     * 
     * @param stashManager The stash manager.
     * @param logger       The logger.
     */
    @Autowired
    public CreateCommand(StashManager stashManager, Logger logger) {
        this.stashManager = stashManager;
        this.logger = logger;
    }

    /**
     * Executes the CREATE command. Creates a new stash. Returns null if there aren't
     * enough tokens. Returns an error response if the stash name is too long or if
     * the stash's name is already taken. Responds with OK.
     * 
     * Format: CREATE <name>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */

        String name = tokens.pollFirst();
        if (name.length() > Stash.MAX_NAME_SIZE) {
            logger.debug(String.format("CREATE %s (failed, name exceeds size limit)", name));
            return ProtocolUtil.buildErrorResponse("The name exceeds the size limit.");
        }

        if (stashManager.containsStash(name)) {
            logger.debug(String.format("CREATE %s (failed, name already taken)", name));
            return ProtocolUtil.buildErrorResponse("The stash name is already taken.");
        }

        stashManager.createStash(name); /* Create the stash */
        
        logger.debug(String.format("CREATE %s", name));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command name.
     */
    public String getName() {
        return NAME;
    }
}
