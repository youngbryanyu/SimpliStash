package com.youngbryanyu.burgerdb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client handler class that handles communication with each client.
 */
public class ClientHandler implements Runnable {
    /**
     * The client socket
     */
    private Socket clientSocket;

    /**
     * Constructor for {@link ClientHandler}
     * 
     * @param clientSocket The client socket
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Handles input from the client and output to the client.
     */
    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);) {

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.printf("Received input from client at port %d: %s\n", clientSocket.getLocalPort(),
                        inputLine);
                out.printf("Server received the message\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSocket(); 
        }
    }

    /**
     * Closes the client socket.
     */
    private void closeSocket() {
        try {
            if (clientSocket != null && clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("IOException occurred while closing the client socket: ");
            e.printStackTrace();
        }
    }
}
