package com.youngbryanyu.burgerdb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class that represents the TCP server used to communicate through TCP protocol with clients.
 */
public class TcpServerHandler {
    /**
     * Port that the server listens on
     */
    private int port;

    /**
     * Constructor for {@link TcpServerHandler}
     * @param port Port that the server listens on
     */
    public TcpServerHandler(int port) {
        this.port = port;
    }

    /**
     * Starts the TCP server and listens for connections from clients.
     */
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("TCP Server started on port: %d\n", port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Client handler class that handles communication with each client.
     */
    private class ClientHandler implements Runnable {
        /**
         * The client socket
         */
        private Socket clientSocket;

        /**
         * Constructor for {@link ClientHandler}
         * @param clientSocket The client socket
         */
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        /**
         * Handles communication between the client and server.
         */
        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.printf("Received input from client at port %d: %s\n", clientSocket.getLocalPort(), inputLine);
                    out.printf("Server at port %d received the message\n", port);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
