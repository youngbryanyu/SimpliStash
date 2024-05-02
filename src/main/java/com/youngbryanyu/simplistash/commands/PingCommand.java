package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The PING command. Replies with a PONG.
 */
@Component
public class PingCommand implements Command {
    /**
     * Name of the command.
     */
    private static final String NAME = "PING";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "PING";
    /**
     * The minimum number of required arguments.
     */
    private static final int MIN_REQUIRED_ARGS = Command.getMinRequiredArgs(FORMAT);
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the PING command.
     * 
     * @param logger The application logger.
     */
    @Autowired
    public PingCommand(Logger logger) {
        this.logger = logger;
    }

    /**
     * Executes the PING command. The token size check shouldn't matter since there
     * would have to be 1 token for this method to be invoked in the first place,
     * but it's here to be defensive.
     * 
     * Format: PING
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }
        
        tokens.pollFirst(); /* Remove command token */

        logger.debug("PING");
        return ProtocolUtil.buildPongResponse(); /* Respone with PONG */
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
