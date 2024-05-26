package com.youngbryanyu.simplistash.commands.write;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * The CREATE command. Creates a new stash.
 */
@Component
public class CreateCommand implements Command {
    /**
     * The command's name.
     */
    public static final String NAME = "CREATE";
    /**
     * The command's format.
     */
    private static final String FORMAT = "CREATE <name> <num_opt_args> [OFF_HEAP] [MAX_KEYS] [SNAPSHOTS]";
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
        OFF_HEAP, /* Must be any case of "true" to be true */
        MAX_KEYS,
        SNAPSHOTS /* Must be any case of "true" to be true */
    }

    /**
     * Constructor for the CREATE command.
     * 
     * @param stashManager The stash manager.
     */
    @Autowired
    public CreateCommand(StashManager stashManager) {
        this.stashManager = stashManager;
        minRequiredArgs = ProtocolUtil.getMinRequiredArgs(FORMAT);
    }

    /**
     * Executes the CREATE command. Returns null if there aren't enough tokens.
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
        String name = tokens.pollFirst();
        String numOptionalArgsStr = tokens.pollFirst();

        /* Get number of optional args */
        int numOptionalArgs = getNumOptionalArgs(numOptionalArgsStr);
        if (numOptionalArgs == -1) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));
        }

        /* Check if there are enough tokens for optional args */
        if (tokens.size() < numOptionalArgs) {
            tokens.addFirst(numOptionalArgsStr);
            tokens.addFirst(name);
            tokens.addFirst(NAME);
            return null;
        }

        /* Check if client is read-only */
        if (readOnly) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.READ_ONLY_MODE));
        }

        /* Validate stash name */
        if (name.length() > Stash.MAX_NAME_LENGTH) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_NAME_TOO_LONG));
        } else if (stashManager.containsStash(name)) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_NAME_TAKEN));
        }

        /* Process optional args */
        Map<String, String> optionalArgVals = processOptionalArgs(tokens, numOptionalArgs);
        if (optionalArgVals == null) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.MALFORMED_OPTIONAL_ARGS));
        }

        /* Determine whether to use off-heap storage (optional arg) */
        boolean offHeap = StashManager.USE_OFF_HEAP_MEMORY;
        if (optionalArgVals.containsKey(OptionalArg.OFF_HEAP.name())) {
            offHeap = Boolean.parseBoolean(optionalArgVals.get(OptionalArg.OFF_HEAP.name()));
        }

        /* Determine whether to enable snapshots (optional arg) */
        boolean enableSnapshots = false;
        if (optionalArgVals.containsKey(OptionalArg.SNAPSHOTS.name())) {
            enableSnapshots = Boolean.parseBoolean(optionalArgVals.get(OptionalArg.SNAPSHOTS.name()));
        }

        /* Get max keys allowed (optional arg) */
        long maxKeyCount = Stash.DEFAULT_MAX_KEY_COUNT;
        if (optionalArgVals.containsKey(OptionalArg.MAX_KEYS.name())) {
            try {
                maxKeyCount = Long.parseLong(optionalArgVals.get(OptionalArg.MAX_KEYS.name()));
            } catch (NumberFormatException e) {
                return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.MAX_KEY_COUNT_INVALID_LONG));
            }

            // Max is Long.MAX_VALUE so going above causes NumberFormatException. The 2nd
            // block of the if statement would never be true (currenlty commented out).
            // if (maxKeyCount <= 0 || maxKeyCount > Command.MAX_KEY_COUNT_LIMIT) {
            if (maxKeyCount <= 0) {
                return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.MAX_KEY_COUNT_OUT_OF_RANGE));
            }
        }

        /* Create stash */
        boolean createdSuccessfully = stashManager.createStash(name, offHeap, maxKeyCount, enableSnapshots);
        if (!createdSuccessfully) {
            return ProtocolUtil.buildErrorResponse(buildErrorMessage(ErrorCause.STASH_LIMIT_REACHED));
        }

        /* Forward to replica */
        stashManager.forwardCommandToReadReplicas(ProtocolUtil.encode(NAME, List.of(name), true, optionalArgVals));

        /* Build response */
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Returns the command's name.
     * 
     * @return THe command's name.
     */
    public String getName() {
        return NAME;
    }
}
