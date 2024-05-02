package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The PING command. Replies with a PONG.
 */
@Component
public class PingCommand implements ReadCommand {
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
     * Executes the PING command. Returns PONG.
     * 
     * Format: PING
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens) {
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

        /* Respond with PONG */
        logger.debug("PING");
        return ProtocolUtil.buildPongResponse();
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
