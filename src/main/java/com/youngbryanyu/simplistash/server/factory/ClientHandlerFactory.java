package com.youngbryanyu.simplistash.server.factory;

import java.net.Socket;

import com.youngbryanyu.simplistash.server.ClientHandler;

/**
 * Factory class used to create client handler objects.
 */
public class ClientHandlerFactory {
    /**
     * Creates a server socket object given the input client socket.
     */
    public static ClientHandler createClientHandler(Socket clientSocket) {
        return new ClientHandler(clientSocket);
    }
}
