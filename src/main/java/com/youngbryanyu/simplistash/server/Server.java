package com.youngbryanyu.simplistash.server;

/**
 * Interface for a client-facing server
 */
public interface Server {
    /**
     * The port to use for the main server.
     */
    public static final int DEFAULT_PRIMARY_PORT = 3000; 
    /**
     * The port to use for the server that can only perform reads.
     */
    public static final int DEFAULT_READ_ONLY_PORT = 3001;    
    /**
     * The max number of connections allowed concurrently with the primary server.
     */
    public static final int MAX_CONNECTIONS_PRIMARY = 1000;
    /**
     * The max number of connections allowed concurrently with the read only server.
     */
    public static final int MAX_CONNECTIONS_READ_ONLY = 1000;
    /**
     * The number of "clients" that are allowed to connect to the server that
     * handles writes. Is 1 for read replicas since only the master node should be
     * able to connect and forward writes.
     */
    public static final int REPLICA_PRIMARY_CONNECTION_LIMIT = 1;

    /**
     * Starts the server.
     * 
     * @throws Exception If an exception is thrown while starting the server.
     */
    public void start() throws Exception;

    /**
     * Increments the number of client connections.
     * 
     * @return False if the max number of connections has been reached, true
     *         otherwise.
     */
    public boolean incrementConnections();

    /**
     * Decrements the number of client connections.
     */
    public void decrementConnections();

    /**
     * Returns the port the server is listening on.
     * 
     * @return The port the server is listening on.
     */
    public int getPort();

    /**
     * Returns the max number of connections
     * @return The max number of connections.
     */
    public int getMaxConnections();
}
