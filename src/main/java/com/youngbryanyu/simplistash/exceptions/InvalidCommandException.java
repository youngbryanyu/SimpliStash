package com.youngbryanyu.simplistash.exceptions;

/**
 * Exception thrown when the command sent by the client is invalid.
 */
public class InvalidCommandException extends Exception {
    /**
     * Constructor for an invalid command exception.
     * 
     * @param command The command sent by the client that was invalid.
     */
    public InvalidCommandException(String command) {
        super(String.format("The command %s is not a valid command.", command));
    }
}
