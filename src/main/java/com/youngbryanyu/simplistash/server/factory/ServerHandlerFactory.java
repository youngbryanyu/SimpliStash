package com.youngbryanyu.simplistash.server.factory;

import java.net.ServerSocket;

import com.youngbryanyu.simplistash.server.ServerHandler;

/**
 * Factory class used to create server handler objects.
 */
public class ServerHandlerFactory {
    /**
     * Creates a server handler object given the input server socket.
     */
    public static ServerHandler createServerHandler(ServerSocket serverSocket) {
        return new ServerHandler(serverSocket);
    }
}