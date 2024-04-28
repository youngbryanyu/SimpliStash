package com.youngbryanyu.simplistash.commands;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Commands used in the protocol.
 */
public enum Command {
    PING,
    SET,
    GET,
    DELETE,

    /* To operate on specific stashes */
    CREATE,
    DESTROY,
    SET_S,
    GET_S,
    DELETE_S;

    /**
     * Gets the enum value matching an input string. Throws an
     * {@link InvalidCommandException} if the command doesn't exist in the set of
     * enums. The commands are non-case-sensitive.
     * 
     * @param s String input command string.
     * @return Returns the matching command enum.
     * @throws InvalidCommandException If the input command is invalid.
     */
    public static Command fromString(String s) throws InvalidCommandException {
        for (Command command : Command.values()) {
            if (command.name().equalsIgnoreCase(s)) {
                return command;
            }
        }

        throw new InvalidCommandException(s);
    }
}
