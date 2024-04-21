package com.youngbryanyu.burgerdb.server;

import java.util.concurrent.TimeUnit;

import com.youngbryanyu.burgerdb.exceptions.ServerStartupException;

/**
 * The class encapsulating the servers to communicate with clients.
 */
public class Server {
    /**
     * Port to use for the server.
     */
    private static final int SERVER_PORT = 3000;

    /**
     * Main method that the program runs on.
     * 
     * @param args Command line args.
     */
    public static void main(String[] args) {
        ServerHandler server = new ServerHandler(SERVER_PORT);
        Thread serverThread = new Thread(() -> {
            try {
                server.startServer();
            } catch (ServerStartupException e) {
                e.printStackTrace();
                System.exit(1); /* Exit with error due to server startup failure */
            }
        });

        /* Spin up thread for server */
        serverThread.start();

        /* Shutdown hook to stop server gracefully */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stopServer();
        }));
    }
}
