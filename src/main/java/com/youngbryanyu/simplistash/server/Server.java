package com.youngbryanyu.simplistash.server;

import java.io.IOException;
import java.net.ServerSocket;

import com.youngbryanyu.simplistash.exceptions.ServerStartupException;

/**
 * The class running the server to communicate with clients.
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
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            ServerHandler server = new ServerHandler(serverSocket);
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
        } catch (IOException e) {
            System.out.println("IOException occurred while creating server socket:");
            e.printStackTrace();
            System.exit(1);
        }

    }
}
