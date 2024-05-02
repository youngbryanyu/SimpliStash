package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.read.ReadCommand;
import com.youngbryanyu.simplistash.commands.write.WriteCommand;
import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Class that handles commands from each client. Maintains no state from each
 * client handler, thus we use a singleton command handler.
 */
@Component
public class CommandHandler {
    /**
     * The factory that retrieves commands.
     */
    private final CommandFactory commandFactory;

    /**
     * The constructor for the command handler.
     * 
     * @param commandFactory The command factory.
     */
    @Autowired
    public CommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Loops through the client's tokens and applies any full valid command from the
     * tokens to the in-memory cache. Builds the response accumulated from each
     * command to be sent back to the lient. Returns null if no command was parsed
     * from the input at all and there is no response to send back to the client.
     * 
     * The parsing stops once a full command cannot be reached.
     * 
     * @param tokens   The tokens parsed from the client's input buffer.
     * @param readOnly Flag indicating whether or not the client is in read-only
     *                 mode.
     * @return The accumulation of responses to be sent back to the client, or null
     *         if no command
     *         was handled to indicate to send no response back to the client.
     * @throws ValueTooLargeException If the user attempts to set a key or value to
     *                                a value that's over the limit.
     */
    public String handleCommands(Deque<String> tokens, boolean readOnly) {
        StringBuilder response = new StringBuilder();

        while (!tokens.isEmpty()) {
            try {
                String commandName = tokens.peekFirst(); /* Peek since we might not have all tokens necessary */
                Command command = commandFactory.getCommand(commandName);
                String result;

                /* Handle write and read operations using separate interfaces */
                if (command instanceof WriteCommand) {
                    result = ((WriteCommand) command).execute(tokens, readOnly);
                } else {
                    result = ((ReadCommand) command).execute(tokens);
                }

                /*
                 * Break if result from command is null, since it indicates that there weren't
                 * enough tokens and more input from the client is needed.
                 */
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