package com.youngbryanyu.simplistash.commands;

import java.util.Arrays;

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
     * Gets the minimum number of required token arguments to execute a command.
     * Commands are always in all caps. Required arguments are in <...>, and
     * optional arguments are in [...]
     * 
     * @return The minimum number of required arguments for a command.
     */
    public static int getMinRequiredArgs(String format) {
        return (int) Arrays.stream(format.split(" "))
                .filter(part -> !part.startsWith("["))
                .count();
    }
}
