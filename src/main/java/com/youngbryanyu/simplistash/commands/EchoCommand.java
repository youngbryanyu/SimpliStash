package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The ECHO command. Replies by echoing the input back.
 */
@Component
public class EchoCommand implements Command {
    /**
     * Name of the command.
     */
    private static final String NAME = "ECHO";
    /**
     * The base format of the command
     */
    private static final String FORMAT = "ECHO <text>";
    /**
     * The minimum number of required arguments.
     */
    private static final int MIN_REQUIRED_ARGS = Command.getMinRequiredArgs(FORMAT);
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the ECHO command.
     * 
     * @param logger The application logger.
     */
    @Autowired
    public EchoCommand(Logger logger) {
        this.logger = logger;
    }

    /**
     * Executes the ECHO command. Echos the input sent by the client back.
     * 
     * Format: ECHO <text>
     */
    public String execute(Deque<String> tokens) {
        if (tokens.size() < MIN_REQUIRED_ARGS) {
            return null;
        }

        tokens.pollFirst(); /* Remove command token */
        String text = tokens.pollFirst();

        logger.debug(String.format("ECHO %s", text));
        return ProtocolUtil.buildValueResponse(text); /* Echo the text back */
    }

    /**
     * Returns the command's name.
     */
    public String getName() {
        return NAME;
    }
}
