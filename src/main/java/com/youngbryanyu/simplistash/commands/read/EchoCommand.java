package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
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
    private final int minRequiredArgs;
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
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the ECHO command. Echos the input sent by the client back.
     * 
     * Format: ECHO <text>
     * 
     * @param tokens The client's tokens.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Return null if not enough arguments */
        if (tokens.size() < minRequiredArgs) {
            return null;
        }

        /*
         * Remove all tokens associated with the command. This should be done at the
         * start in order to not pollute future command execution in case the command
         * exits early due to an error.
         */
        tokens.pollFirst();
        String text = tokens.pollFirst();

        /* Build value response */
        logger.debug(String.format("ECHO %s", text));
        return ProtocolUtil.buildValueResponse(text);
    }

    /**
     * Returns the command's name.
     * 
     * @return The command's name.
     */
    public String getName() {
        return NAME;
    }
}
