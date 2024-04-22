package com.youngbryanyu.simplistash.server.factory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Factory class used to create server socket objects
 */
public class ServerSocketFactory {
    /**
     * Creates a server socket object given the input port number.
     */
    public static ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }
}
