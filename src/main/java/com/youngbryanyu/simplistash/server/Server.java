package com.youngbryanyu.simplistash.server;

import java.io.IOException;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
import com.youngbryanyu.simplistash.cache.InMemoryCacheFactory;

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
             * We let most runtime exceptions bubble up to `main`, while catching and
             * handling most checked exceptions.
             */
            System.out.println("Error occurred while running server:");
            e.printStackTrace();

            /*
             * This line will not be covered due to using workarounds with JMockit to mock
             * System.exit during unit tests.
             */
            System.exit(1);
        }
    }

    /**
     * Server startup script that runs all necessary setup and logic to start up and
     * run the server.
     * 
     * @throws IOException if an I/O exception was thrown while instantiating
     *                     {@link SeverHandler}.
     */
    public static void runStartupScript() throws IOException {
        InMemoryCache cache = InMemoryCacheFactory.createInMemoryCache();
        ServerHandler serverHandler = ServerHandlerFactory.createServerHandler(PORT, cache);

        /* Set up server cleanup */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverHandler.stopServer();
        }));

        /* Run server on the main thread */
        serverHandler.startServer();
    }
}
