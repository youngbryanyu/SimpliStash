package com.youngbryanyu.simplistash.cli.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * The CLI command factory.
 */
@Component
public class CLICommandFactory {
    /**
     * Immutable map that maps CLI command names to their object.
     */
    private final Map<String, CLICommand> commands;

    /**
     * Constructor for the CLI command factory.
     * 
     * @param commandList The list of all CLI commands.
     */
    @Autowired
    public CLICommandFactory(List<CLICommand> commandList) {
        commands = new HashMap<>();
        for (CLICommand command : commandList) {
            commands.put(command.getName(), command);
        }
    }

    /**
     * Retrieves the CLI command object matching the command name.
     * 
     * @param name The CLI command's name.
     * @return The CLI command object.
     * @throws InvalidCommandException If the command name doesn't correspond to a
     *                                 valid CLI command.
     */
    public CLICommand getCommand(String name) throws InvalidCommandException {
        CLICommand command = commands.get(name);
        if (command == null) {
            throw new InvalidCommandException(name);
        }
        return command;
    }
}
