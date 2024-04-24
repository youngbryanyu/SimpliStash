package com.youngbryanyu.simplistash.server;

import java.io.IOException;

import com.youngbryanyu.simplistash.cache.InMemoryCache;

/**
 * Factory class for creating {@link ServerHandler}.
 */
public class ServerHandlerFactory {
    /**
     * Private constructor to prevent instantiation.
     */
    private ServerHandlerFactory() {
    }

    /**
     * Creates an instance of {@link ServerHandler}.
     */
    public static ServerHandler createServerHandler(int port, InMemoryCache cache) throws IOException {
        return new ServerHandler(port, cache);
    }
}
