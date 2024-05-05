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
 * The EXPIRE command. Sets a TTL for a key.
 */
@Component
public class ExpireCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "EXPIRE";
    /**
     * The command's format.
     */
    private static final String FORMAT = "EXPIRE <key> <TTL> <num_optional_args> [NAME]";
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
     * Constructor for the EXPIRE command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public ExpireCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the EXPIRE command. Returns null if there aren't enough tokens.
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
        String key = tokens.pollFirst();
        String ttlStr = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);
        if (numOptionalArgs == -1) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, invalid optional args count.");
        }

        /* Check if there are enough tokens for optional args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(ttlStr);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Check if client is read-only */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, read-only mode.");
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);
        if (optionalArgVals == null) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, malformed optional args.");
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
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, stash doesn't exist.");
        }

        /* Get TTL */
        long ttl;
        try {
            ttl = Long.parseLong(ttlStr);
        } catch (NumberFormatException e) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, TTL must be a valid long.");
        }
        if (ttl <= 0 || ttl > Command.MAX_TTL) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, TTL is outside the supported range.");
        }

        /* Check if key exists */
        if (!stash.contains(key)) {
            return ProtocolUtil.buildErrorResponse("EXPIRE failed, the key doesn't exist.");
        }

        /* Update TTL */
        stash.updateTTL(key, ttl);

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
