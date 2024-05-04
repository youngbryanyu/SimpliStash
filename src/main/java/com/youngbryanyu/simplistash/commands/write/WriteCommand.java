package com.youngbryanyu.simplistash.commands.write;

import java.util.Deque;

import com.youngbryanyu.simplistash.commands.Command;

/**
 * Interface for a command that performs a write operation.
 */
public interface WriteCommand extends Command {
    /**
     * Executes a command.
     * 
     * @param tokens The client's tokens
     * @param readOnly Whether or not the client is read-only.
     * @return The response to send to the client.
     */
    public String execute(Deque<String> tokens, boolean readOnly) throws Exception;
}
