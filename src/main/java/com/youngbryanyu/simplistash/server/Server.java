package com.youngbryanyu.simplistash.server;

/**
 * Interface for a client-facing server
 */
public interface Server {
    /**
     * The port to use for the main server.
     */
    public static final int WRITEABLE_PORT = 3000;
    /**
     * The port to use for the server that can only perform reads.
     */
    public static final int READ_ONLY_PORT = 3001;

    /**
     * Starts the server.
     * 
     * @throws Exception If an exception is thrown while starting the server.
     */
    public void start() throws Exception;
}
