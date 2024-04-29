package com.youngbryanyu.simplistash.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

@Component
public class CommandFactory {
    /**
     * Immutable map containing all commands. Do not need to use concurrent hash map
     * since it is immutable.
     */
    private Map<String, Command> commands;

    /**
     * Constructor for the command factory. Maps all command names to their
     * instance. The names are case sensitive. Spring will automatically inject all
     * commands into the List collection.
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
     * Retrieves the command instance matching the command name.
     * 
     * @param name The command's name.
     * @return The command object instance.
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
