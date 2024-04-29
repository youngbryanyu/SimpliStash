package com.youngbryanyu.simplistash.commands;

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
}
