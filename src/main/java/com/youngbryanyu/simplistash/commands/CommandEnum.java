package com.youngbryanyu.simplistash.commands;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

// TODO: replace with command pattern 

/**
 * Commands used in the protocol.
 */
public enum CommandEnum {
    PING,
    SET,
    GET,
    DELETE,

    /* To operate on specific stashes */
    CREATE,
    DROP,
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
    public static CommandEnum fromString(String s) throws InvalidCommandException {
        for (CommandEnum command : CommandEnum.values()) {
            if (command.name().equalsIgnoreCase(s)) {
                return command;
            }
        }

        throw new InvalidCommandException(s);
    }
}
