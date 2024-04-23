package com.youngbryanyu.simplistash.server;

import java.io.IOException;

/**
 * The class running the server to communicate with clients.
 */
public class Server {
    /**
     * Port to use for the server.
     */
    private static final int PORT = 3000;

    /**
     * Main method which the server startup script.
     */
    public static void main(String[] args) {
        try {
            runStartupScript();
        } catch (Exception e) {
            /*
             * We aren't testing this branch of code due to complications with mocking
             * System.exit. We've tried existing libraries but many require the Security
             * Manager which is deprecated starting in Java 17.
             * 
             * We let most runtime exceptions bubble up to `main`, while catching and
             * handling most checked exceptions.
             */
            System.out.println("Error occurred while running server:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Server startup script that runs all necessary setup and logic to start up and
     * run the server.
     * 
     * @throws IOException if an I/O exception was thrown while instantiating {@link SeverHandler}.
     */
    public static void runStartupScript() throws IOException {
        ServerHandler serverHandler = ServerHandlerFactory.createServerHandler(PORT);

        /* Set up server cleanup */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverHandler.stopServer();
        }));

        /* Run server on the main thread */
        serverHandler.startServer();
    }
}
