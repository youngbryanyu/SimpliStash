package com.youngbryanyu.simplistash.exceptions;

/**
 * Exception thrown when the client sends input that doesn't follow the
 * communication protocol.
 */
public class BrokenProtocolException extends Exception {
    /**
     * Constructor for the exception with message.
     * 
     * @param message The error message to send the client.
     * @param cause   The throwable cause.
     */
    public BrokenProtocolException(String message) {
        super(message);
    }

    /**
     * Constructor for the exception with message and cause.
     * 
     * @param message The error message to send the client.
     * @param cause   The throwable cause.
     */
    public BrokenProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
