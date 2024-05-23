package com.youngbryanyu.simplistash;

import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.server.ServerMonitor;
import com.youngbryanyu.simplistash.server.primary.PrimaryServer;
import com.youngbryanyu.simplistash.server.readOnly.ReadOnlyServer;

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
        /* Initialize Spring DI */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Server primaryServer = context.getBean(PrimaryServer.class);
        Server readOnlyServer = context.getBean(ReadOnlyServer.class);
        ServerMonitor serverMonitor = context.getBean(ServerMonitor.class);
        Logger logger = context.getBean(Logger.class);

        /* Setup shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            context.close();
        }));

        
        /* Start application */
        start(primaryServer, readOnlyServer, serverMonitor, logger);
    }

    protected static void start(Server primaryServer, Server readOnlyServer,
            ServerMonitor serverMonitor, Logger logger) {
        /* Create primary server thread */
        Thread primaryServerThread = new Thread(() -> {
            try {
                primaryServer.start();
            } catch (Exception e) {
                serverMonitor.setServerCrashed();
                logger.error("The primary server crashed:", e);
            }
        });

        /* Create read-only server thread */
        Thread readOnlyServerThread = new Thread(() -> {
            try {
                readOnlyServer.start();
            } catch (Exception e) {
                serverMonitor.setServerCrashed();
                logger.error("The read-only server crashed:", e);
            }
        });

        /* Start servers */
        primaryServerThread.start();
        readOnlyServerThread.start();

        try {
            /* Block until one of the servers crashes */
            serverMonitor.waitForCrash();

            /* Interrupt server threads to stop application */
            primaryServerThread.interrupt();
            readOnlyServerThread.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); /* Interrupt the main thread */
            logger.error("The server monitor's thread was interrupted: ", e);
        }
    }
}
