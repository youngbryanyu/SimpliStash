package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The DELETE command. Deletes a key from a stash.
 */
@Component
public class DeleteCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "DELETE";
    /**
     * The command's name.
     */
    private static final String FORMAT = "DELETE <key> <num_optional_args> [NAME]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The name of the optional name arg.
     */
    private static final String ARG_NAME = "NAME";
    /**
     * The stash manager.
     */
    private final StashManager stashManager;

    /**
     * Constructor for the DELETE command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public DeleteCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the DELETE command. Returns null if there aren't enough tokens.
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
        String key = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);
        if (numOptionalArgs == -1) {
            return ProtocolUtil.buildErrorResponse("DELETE failed, invalid optional args count.");
        }

        /* Check if there are enough tokens for optional args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Check if client is read-only */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse("DELETE failed, read-only mode.");
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);
        if (optionalArgVals == null) {
            return ProtocolUtil.buildErrorResponse("DELETE failed, malformed optional args.");
        }

        /* Get stash name */
        String name;
        if (optionalArgVals.containsKey(ARG_NAME)) {
            name = optionalArgVals.get(ARG_NAME);
        } else {
            name = StashManager.DEFAULT_STASH_NAME;
        }

        /* Get stash */
        Stash stash = stashManager.getStash(name);
        if (stash == null) {
            return ProtocolUtil.buildErrorResponse("DELETE failed, stash doesn't exist.");
        }

        /* Delete key */
        stash.delete(key);

        /* Build response */
        return ProtocolUtil.buildOkResponse();
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
