package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The PING command. Replies with a PONG.
 */
@Component
public class PingCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "PING";
    /**
     * The command's format.
     */
    private static final String FORMAT = "PING";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * Constructor for the PING command.
     */
    @Autowired
    public PingCommand() {
        minRequiredArgs = ProtocolUtil.getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the PING command. Returns null if there aren't enough tokens.
     * 
     * @param tokens   The client's tokens.
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

        /* Build response */
        return ProtocolUtil.buildPongResponse();
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
