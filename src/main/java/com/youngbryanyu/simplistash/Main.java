package com.youngbryanyu.simplistash;

import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;

/**
 * The entry point to the application.
 */
public class Main {
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

            /* DBs within each stash have their own shutdown hook set when created */
        }));

        try {
            server.start();
        } catch (Exception e) {
            logger.error("The server failed while running:");
            e.printStackTrace();
        }
    }
}
