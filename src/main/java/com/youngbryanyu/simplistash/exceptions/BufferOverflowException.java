package com.youngbryanyu.simplistash.exceptions;

/**
 * Exception thrown when the client's input buffer exceeds the maximum allowable size.
 */
public class BufferOverflowException extends Exception {
    /**
     * Constructor for the exception with message.
     * 
     * @param message The error message to send the client.
     * @param cause   The throwable cause.
     */
    public BufferOverflowException(String message) {
        super(message);
    }
}
