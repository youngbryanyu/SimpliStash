package com.youngbryanyu.simplistash.protocol;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Commands used in the protocol.
 */
public enum Command {
    SET,
    GET,
    DELETE,
    PING;

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

    /**
     * Returns whether an input string matches one of the command enums.
     * 
     * @param s String input command string.
     * @return Returns whether the input string matches an enum.
     */
    public static boolean contains(String s) {
        for (Command command : Command.values()) {
            if (command.name().equalsIgnoreCase(s)) {
                return true;
            }
        }

        return false;
    }
}
