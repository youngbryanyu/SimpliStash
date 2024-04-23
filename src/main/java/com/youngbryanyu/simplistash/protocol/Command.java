package com.youngbryanyu.simplistash.protocol;

/**
 * Commands used in the protocol.
 */
public enum Command {
    SET,
    GET,
    DELETE;

    /**
     * Gets the enum value matching an input string.
     * @param input String input command string.
     * @return Returns the matching command enum.
     */
    public static Command fromString(String input) {
        for (Command command : Command.values()) {
            if (command.name().equalsIgnoreCase(input)) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + input);
    }
}
