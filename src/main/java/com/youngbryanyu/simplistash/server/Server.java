package com.youngbryanyu.simplistash.server;

import java.io.IOException;
import java.net.ServerSocket;

import com.youngbryanyu.simplistash.exceptions.ServerStartupException;
import com.youngbryanyu.simplistash.server.factory.ServerHandlerFactory;
import com.youngbryanyu.simplistash.server.factory.ServerSocketFactory;

/**
 * The class running the server to communicate with clients.
 */
public class Server {
    /**
     * Port to use for the server.
     */
    public static final int PORT = 3000;

    /**
     * Main method that the program runs on.
     */
    public static void main(String[] args) {
        try {
            runServerScript();
        } catch (IOException | ServerStartupException e) {
            /* We don't test this branch of code due to complications with mocking System.exit */
            System.out.println("Error occurred while starting server: ");
            e.printStackTrace();
            System.exit(1); 
        }
    }

    /**
     * Runs all necessary code to start up and run the server.
     */
    public static void runServerScript() throws IOException, ServerStartupException {
        ServerSocket serverSocket = ServerSocketFactory.createServerSocket(PORT);
        final ServerHandler server = ServerHandlerFactory.createServerHandler(serverSocket);

        /* Set up server cleanup */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stopServer();
        }));
        
        /* Run server on the main thread */
        server.startServer();
    }
}
