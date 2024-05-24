package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The STATS command. Gets stats such as memory and disk availability.
 */
@Component
public class StatsCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "STATS";
    /**
     * The command's format.
     */
    private static final String FORMAT = "STATS";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The stash manager.
     */
    private final StashManager stashManager;

    /**
     * The optional args.
     */
    public enum OptionalArg {
        NAME;
    }

    /**
     * Constructor for the STATS command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public StatsCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = ProtocolUtil.getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the INFO command. Returns null if there aren't enough tokens.
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

        /* Get stats */
        String stats = stashManager.getStats();

        /* Build response */
        return ProtocolUtil.buildValueResponse(stats);
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
