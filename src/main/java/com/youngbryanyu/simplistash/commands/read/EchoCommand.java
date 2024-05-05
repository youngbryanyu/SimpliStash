package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

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
     * The command's name.
     */
    private static final String NAME = "ECHO";
    /**
     * The command's format.
     */
    private static final String FORMAT = "ECHO <text>";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * Constructor for the ECHO command.
     */
    @Autowired
    public EchoCommand() {
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the ECHO command. Returns null if there aren't enough tokens.
     * 
     * @param tokens The client's tokens.
     * @param readOnly Whether the client is read-only.
     * @return The response to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) {
        /* Check if there are enough tokens */
        if (tokens.size() < minRequiredArgs) {
            return null;
        }

        /* Extract tokens */
        tokens.pollFirst();
        String text = tokens.pollFirst();

        /* Build response */
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
