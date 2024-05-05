package com.youngbryanyu.simplistash.commands;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.Nullable;

/**
 * Interface for a command.
 */
public interface Command {
    /**
     * The maximum allowed TTL in milliseconds
     */
    public static final long MAX_TTL = 157_784_630_000L;

    /**
     * Returns the name of the command.
     * 
     * @return The command's name.
     */
    public String getName();

    /**
     * Executes a command.
     * 
     * @param tokens   The client's tokens
     * @param readOnly Whether or not the client is read-only.
     * @return The response to send to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly);

    /**
     * Gets the minimum number of required arguments to execute a command.
     * Commands are always in all caps. Required arguments are in <...>, and
     * optional arguments are in [...].
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
     * Each arg must be in the form <name>=<value>. Returns null if any of the arguments were malformed.
     * 
     * @param tokens  The client's tokens.
     * @param numArgs The number of optional args.
     * @return A map of arg names to arg vals.
     */
    @Nullable
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
}
