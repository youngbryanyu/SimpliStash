package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;
/**
 * Class containing methods to help parse the client's input from their buffer
 * into tokens, and handle any commands by applying them to the cache provided.
 */
@Component
public class CommandHandler {
    /**
     * The command factory to get commands.
     */
    private final CommandFactory commandFactory;

    /**
     * Private constructor to prevent instantiation.
     */
    @Autowired
    public CommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Applies any full valid command from the parsed tokens to the in-memory cache.
     * Returns the response to be sent back to the client. Returns null if no
     * command was parsed from the input at all and there is nothing to send back to
     * the client.
     * 
     * The parsing stops once a full command cannot be reached.
     * 
     * @param tokens The tokens parsed from the client's input buffer.
     * @return The response to be sent back to the client, or `null` if no command
     *         was handled and executed to indicate to the caller that no response
     *         is needed.
     * @throws ValueTooLargeException If the user attempts to set a key or value to
     *                                a value that's over the limit.
     */
    public String handleCommands(Deque<String> tokens) {
        StringBuilder response = new StringBuilder();

        while (!tokens.isEmpty()) {
            try {
                String commandName = tokens.peekFirst(); /* Peek since we might not have all args necessary */
                Command command = commandFactory.getCommand(commandName);
                String result = command.execute(tokens);

                /* Null indicates not enough tokens left so we break */
                if (result == null) {
                    break;
                }

                response.append(result);
            } catch (InvalidCommandException e) {
                tokens.pollFirst(); /* Discard invalid command token */
                continue;
            }
        }

        return response.isEmpty() ? null : response.toString();
    }
}