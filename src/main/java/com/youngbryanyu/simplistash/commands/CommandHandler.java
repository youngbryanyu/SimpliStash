package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Class that handles commands from each client.
 */
@Component
public class CommandHandler {
    /**
     * The factory that retrieves commands.
     */
    private final CommandFactory commandFactory;
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * The constructor for the command handler.
     * 
     * @param commandFactory The command factory.
     */
    @Autowired
    public CommandHandler(CommandFactory commandFactory, Logger logger) {
        this.commandFactory = commandFactory;
        this.logger = logger;
    }

    /**
     * Loops through the client's tokens and applies all full valid commands from
     * the tokens. Returns null if no command was executed.
     * 
     * @param tokens   The client's tokens.
     * @param readOnly Whether the client is in read-only mode.
     * @return The responses to the client.
     */
    public String handleCommands(Deque<String> tokens, boolean readOnly) {
        StringBuilder response = new StringBuilder();

        while (!tokens.isEmpty()) {
            try {
                /* Execute command */
                String commandName = tokens.peekFirst();
                Command command = commandFactory.getCommand(commandName);
                String result = command.execute(tokens, readOnly);

                logger.debug(String.format("Executing command: \n" +
                        "- %s\n" +
                        "- readOnly: %b\n" +
                        "- Result: %s\n",
                        command.getName(), readOnly, result == null ? "null" : result));

                /* Check if result is null indicating no commands were executed */
                if (result == null) {
                    break;
                }

                /* Build response */
                response.append(result);
            } catch (InvalidCommandException e) {
                tokens.pollFirst(); /* Discard invalid command token */
                continue;
            }
        }

        return response.isEmpty() ? null : response.toString(); /* Return null if no response */
    }
}