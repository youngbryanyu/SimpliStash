package com.youngbryanyu.simplistash.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.youngbryanyu.simplistash.exceptions.ServerStartupException;
import com.youngbryanyu.simplistash.server.factory.ClientHandlerFactory;

/**
 * Class that represents the server used to communicate with clients.
 */
public class ServerHandler {
    /**
     * The server socket that accepts connections.
     */
    private ServerSocket serverSocket;

    /**
     * Flag indicated whether the server is running.
     */
    private volatile boolean running;

    /**
     * Constructor for {@link ServerHandler}
     * 
     * @param port Port that the server listens on.
     */
    public ServerHandler(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.running = false;
    }

    /**
     * Starts the server.
     */
    public void startServer() throws ServerStartupException {
        try {
            serverSocket.setReuseAddress(true); /* Enable quick rebinding to the port */
            this.running = true;
            System.out.printf("Server started on port: %d\n", serverSocket.getLocalPort());

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = ClientHandlerFactory.createClientHandler(clientSocket);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (!running) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ServerStartupException("IOException occurred while starting the server. ", e);
        } finally {
            /*
             * This call to stopServer is probably redundant, since the server's loop will
             * only exit and reach this finally block after stopServer is already called
             * once in the shutdown hook. However, we keep this as a backup.
             */
            stopServer();
        }
    }

    /**
     * Stops the server and close its socket.
     */
    public void stopServer() {
        this.running = false;

        if (serverSocket != null) {
            try {
                serverSocket.close(); /* Force blocking call serverSocket.accept() to throw SocketException */
            } catch (IOException e) {
                System.out.println("IOException occurred while closing the server socket: ");
                e.printStackTrace();
            }
        }
    }
}
