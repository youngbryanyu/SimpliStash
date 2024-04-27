package com.youngbryanyu.simplistash;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.stash.InMemoryCache;

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
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        /* Bootstrap Spring Context */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Server server = context.getBean(Server.class);
        Logger logger = context.getBean(Logger.class);

        /* Cleanup resources on shutdown */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            context.close();
        }));

        try {
            server.start();
        } catch (Exception e) {
            logger.error("The server failed while running:");
            e.printStackTrace();
        }
    }
}
