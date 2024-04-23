package com.youngbryanyu.simplistash.server;

import java.io.IOException;

import com.youngbryanyu.simplistash.cache.KeyValueStore;

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
    public static ServerHandler createServerHandler(int port, KeyValueStore keyValueStore) throws IOException {
        return new ServerHandler(port, keyValueStore);
    }
}
