package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The PING command.
 */
@Component
public class PingCommand implements Command {
    /**
     * Name of the command.
     */
    public static final String NAME = "PING";
    /**
     * The application logger.
     */
    private Logger logger;

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
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < 1) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */

        logger.info("Command: PING");
        return ProtocolUtil.buildPongResponse();
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
