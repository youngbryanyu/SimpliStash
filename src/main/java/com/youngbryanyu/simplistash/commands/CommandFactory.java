package com.youngbryanyu.simplistash.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * The factory class to retrieve concrete command classes that implement the
 * command interface.
 */
@Component
public class CommandFactory {
    /**
     * Immutable map that maps command names to their object.
     */
    private final Map<String, Command> commands;

    /**
     * Constructor for the command factory.
     * 
     * @param commandList The list of all commands.
     */
    @Autowired
    public CommandFactory(List<Command> commandList) {
        commands = new HashMap<>();
        for (Command command : commandList) {
            commands.put(command.getName(), command);
        }
    }

    /**
     * Retrieves the command object matching the command name.
     * 
     * @param name The command's name.
     * @return The command object.
     * @throws InvalidCommandException If the command name doesn't correspond to a
     *                                 valid command.
     */
    public Command getCommand(String name) throws InvalidCommandException {
        Command command = commands.get(name);
        if (command == null) {
            throw new InvalidCommandException(name);
        }
        return command;
    }
}
