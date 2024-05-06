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
 * The SET command. Sets a key's value in a stash.
 */
@Component
public class SetCommand implements Command {
    /**
     * The command's name.
     */
    private static final String NAME = "SET";
    /**
     * The command's format.
     */
    private static final String FORMAT = "SET <key> <value> <num_optional_args> [NAME] [TTL]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;
    /**
     * The name of the optional name arg.
     */
    private static final String ARG_NAME = "NAME";
    /**
     * The name of the optional ttl arg.
     */
    private static final String ARG_TTL = "TTL";
    /**
     * The stash manager.
     */
    private final StashManager stashManager;

    /**
     * Constructor for the SET command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public SetCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = getMinRequiredArgs(FORMAT);
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
        String key = tokens.pollFirst();
        String value = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);
        if (numOptionalArgs == -1) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));
        }

        /* Check if there are enough tokens for optional args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(value);
            tokens.addFirst(key);
            tokens.addFirst(NAME);
            return null;
        }

        /* Check if client is read-only */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.READ_ONLY_MODE));
        }

        /* Validate key */
        if (key.length() > Stash.MAX_KEY_LENGTH) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.KEY_TOO_LONG));
        } else if (value.length() > Stash.MAX_VALUE_LENGTH) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.VALUE_TOO_LONG));
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);
        if (optionalArgVals == null) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.MALFORMED_OPTIONAL_ARGS));
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
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_DOESNT_EXIST));
        }

        /* Set TTL (optional) */
        long ttl = -1;
        if (optionalArgVals.containsKey(ARG_TTL)) {
            try {
                ttl = Long.parseLong(optionalArgVals.get(ARG_TTL));
            } catch (NumberFormatException e) {
                return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.TTL_INVALID_LONG));
            }

            if (ttl <= 0 || ttl > Command.MAX_TTL) {
                return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.TTL_OUT_OF_RANGE));
            }
        }

        /* Set value */
        if (ttl == -1) {
            stash.set(key, value);
        } else {
            stash.setWithTTL(key, value, ttl); /* Set with TTL if specified */
        }

        /* Build response */
        return ProtocolUtil.buildOkResponse();
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
