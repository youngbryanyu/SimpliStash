package com.youngbryanyu.simplistash.commands;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for a command.
 */
public interface Command {
    /**
     * The maximum allowed TTL in milliseconds.
     */
    public static final long MAX_TTL = 157_784_630_000L;

    /**
     * Enum for command error causes, which are part of error messages to return to
     * clients.
     */
    public enum ErrorCause {
        /* Optional arg errors */
        INVALID_OPTIONAL_ARGS_COUNT("Invalid optional args count"),
        MALFORMED_OPTIONAL_ARGS("Malformed optional args"),
        /* Read-only mode errors */
        READ_ONLY_MODE("Cannot perform this action in read-only mode"),
        /* Key/value errors */
        KEY_TOO_LONG("The key is too long"),
        VALUE_TOO_LONG("The value is too long"),
        KEY_DOESNT_EXIST("The key doesn't exist"),
        /* Stash errors */
        STASH_DOESNT_EXIST("Stash doesn't exist"),
        STASH_CANNOT_DROP_DEFAULT("Cannot drop the default stash"),
        STASH_NAME_TOO_LONG("The stash name is too long"),
        STASH_NAME_TAKEN("The stash name is already taken"),
        STASH_LIMIT_REACHED("The max number of stashes has been reached"),
        /* TTL errors */
        TTL_INVALID_LONG("The TTL must be a valid long"),
        TTL_OUT_OF_RANGE("The TTL is out of the supported range");

        /**
         * The enum's message
         */
        private final String message;

        /**
         * 
         * @param message
         */
        private ErrorCause(String message) {
            this.message = message;
        }

        /**
         * Returns the message associated with the error cause enum.
         * 
         * @return The error cause message.
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Returns the command's name.
     * 
     * @return The command's name.
     */
    public String getName();

    /**
     * Executes a command.
     * 
     * @param tokens   The client's tokens
     * @param readOnly Whether the client is read-only.
     * @return The response to send to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly);

    /**
     * Gets the minimum number of required arguments to execute a command. Required
     * arguments are in <...>, and optional arguments are in [...].
     * 
     * @return The minimum number of required arguments for a command.
     */
    public default int getMinRequiredArgs(String format) {
        return (int) Arrays.stream(format.split(" "))
                .filter(part -> !part.startsWith("["))
                .count();
    }

    /**
     * Returns the number of optional arguments. Returns -1 if the input is
     * malformed.
     * 
     * @param token The token representing the number of args.
     * @return The number of optional arguments.
     */
    public default int getNumOptionalArgs(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Process the optional args for a command and store them in a map.
     * 
     * Each arg must be in the form <name>=<value>. Returns null if any of the
     * arguments were malformed.
     * 
     * @param tokens  The client's tokens.
     * @param numArgs The number of optional args.
     * @return A map of arg names to arg vals.
     */
    public default Map<String, String> processOptionalArgs(Deque<String> tokens, int numArgs) {
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < numArgs; i++) {
            String token = tokens.poll();
            String[] arg = token.split("=");

            /* Return null if arg is malformed */
            if (arg.length != 2) {
                return null;
            }

            argMap.put(arg[0], arg[1]);
        }

        return argMap;
    }

    /**
     * Builds an error message for the command.
     * 
     * @param cause The cause of the error.
     * @return The error message for the command with cause.
     */
    public default String buildErrorMessage(ErrorCause cause) {
        return String.format("%s failed: %s", getName(), cause.getMessage());
    }
}
