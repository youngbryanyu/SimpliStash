package com.youngbryanyu.simplistash;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
import com.youngbryanyu.simplistash.server.Server;

/**
 * The entry point to the application.
 */
public class Main {
    /**
     * The port that the server listens on.
     */
    private static final int PORT = 3000;

    /**
     * The main method which starts the server.
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) {        
        InMemoryCache cache = new InMemoryCache();
        
        try {
            new Server(PORT, cache).start();
        } catch (Exception e) {
            System.out.println("The server failed to start:");
            e.printStackTrace();
        } 
    }
}
