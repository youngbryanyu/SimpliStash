package com.youngbryanyu.simplistash.commands.replica;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The REPLICA command. Registers a read replica
 */
@Component
public class ReplicaCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "REPLICA";
    /**
     * The command's format.
     */
    private static final String FORMAT = "REPLICA <ip> <port>";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The stash manager.
     */
    private final StashManager stashManager;

    /**
     * Constructor for the REPLICA command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public ReplicaCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = ProtocolUtil.getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the SET command. Returns null if there aren't enough tokens.
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
        String ip = tokens.pollFirst();
        String portString = tokens.pollFirst();
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.INVALID_PORT));
        }

        /* Register as read replica */
        stashManager.registerReadReplica(ip, port);

        /* Return null, no response needed */
        return null;
    }

    /**
     * Returns the command's name.
     * 
     * @return The command name
     */
    public String getName() {
        return NAME;
    }
}
