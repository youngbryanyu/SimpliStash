package com.youngbryanyu.simplistash.exceptions;

/**
 * Exception thrown when the client sends input that doesn't follow the
 * communication protocol. This exception should typically cause the client to
 * be disconnected.
 */
public class BrokenProtocolException extends Exception {
    /**
     * Error cause when the token size is an invalid integer.
     */
    public static final String TOKEN_SIZE_INVALID_INTEGER = "The token size is not a valid integer";
     /**
     * Error cause when the token size is out of range.
     */
    public static final String TOKEN_SIZE_OUT_OF_RANGE = "The token size must be at least 1";
    
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
        super(String.format("Protocol error: %s, disconnecting...", message), cause);
    }
}
