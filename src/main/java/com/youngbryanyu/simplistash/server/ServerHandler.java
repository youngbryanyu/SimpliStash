package com.youngbryanyu.simplistash.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.youngbryanyu.simplistash.exceptions.ServerStartupException;

/**
 * Class that represents the server used to communicate with clients.
 */
public class ServerHandler {
    /**
     * Port that the server listens on.
     */
    private int port;

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
    public ServerHandler(int port) {
        this.port = port;
        this.running = false;
    }

    /**
     * Starts the TCP server.
     */
    public void startServer() throws ServerStartupException {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true); /* Enable quick rebinding to the port */
            this.running = true;
            System.out.printf("TCP server started on port: %d\n", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    new Thread(clientHandler).start();
                } catch (SocketException e) {
                    if (!running) {
                        break;
                    }
                } 
            }
        } catch (IOException e) {
            throw new ServerStartupException("IOException occurred while starting the server. ", e);
        } finally {
            stopServer();
        }
    }

    /**
     * Stops the TCP server and close its socket.
     */
    public void stopServer() {
        this.running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close(); /* Force blocking call serverSocket.accept() to throw SocketException */
            } catch (IOException e) {
                System.out.println("IOException occurred while closing the server socket: ");
                e.printStackTrace();
            }
        }
    }
}
