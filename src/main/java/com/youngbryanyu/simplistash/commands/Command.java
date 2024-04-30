package com.youngbryanyu.simplistash.commands;

import java.util.Arrays;
import java.util.Deque;

/**
 * Interface for a command.
 */
public interface Command {
    /**
     * Executes a command.
     * 
     * @param tokens The client's tokens
     * @return The response to send to the client.
     */
    public String execute(Deque<String> tokens);

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
