package com.youngbryanyu.simplistash.server;

import java.io.IOException;

/**
 * Factory class for creating {@link ServerHandler}.
 */
public class ServerHandlerFactory {
    /**
     * Creates an instance of {@link ServerHandler}.
     */
    public static ServerHandler createServerHandler(int port) throws IOException {
        return new ServerHandler(port);
    }
}
