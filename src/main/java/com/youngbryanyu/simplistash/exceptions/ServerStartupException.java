package com.youngbryanyu.simplistash.exceptions;

/**
 * Exception thrown when the servers fails to startup.
 */
public class ServerStartupException extends Exception {
    /**
     * Constructor for {@link ServerStartupException}.
     * @param message Message associated with the exception.
     * @param cause The exception causing this to be thrown.
     */
    public ServerStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
