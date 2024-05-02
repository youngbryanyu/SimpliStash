package com.youngbryanyu.simplistash.commands.read;

import java.util.Deque;

import com.youngbryanyu.simplistash.commands.Command;

/**
 * Interface for a command that performs a read operation.
 */
public interface ReadCommand extends Command {
    /**
     * Executes a command.
     * 
     * @param tokens The client's tokens
     * @return The response to send to the client.
     */
    public String execute(Deque<String> tokens);
}
