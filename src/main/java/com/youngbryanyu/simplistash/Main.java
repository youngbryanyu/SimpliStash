package com.youngbryanyu.simplistash;

import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.ReadOnlyServer;
import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.server.ServerMonitor;
import com.youngbryanyu.simplistash.server.WriteableServer;
import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

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
        TTLTimeWheel ttlTimeWheel = new TTLTimeWheel();
        for (int i = 0; i < 10; i++) {
            ttlTimeWheel.add("key" + i, i * 100);
        }

        /* Initialize Spring DI */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Server writeableServer = context.getBean(WriteableServer.class);
        Server readOnlyServer = context.getBean(ReadOnlyServer.class);
        ServerMonitor serverMonitor = context.getBean(ServerMonitor.class);
        Logger logger = context.getBean(Logger.class);

        /* Setup shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            context.close();
        }));

        /* Start application */
        start(writeableServer, readOnlyServer, serverMonitor, logger);
    }

    public static void start(Server writeableServer, Server readOnlyServer,
            ServerMonitor serverMonitor, Logger logger) {
        /* Create writeable server thread */
        Thread writeableServerThread = new Thread(() -> {
            try {
                writeableServer.start();
            } catch (Exception e) {
                serverMonitor.setServerCrashed();
                logger.error("The writeable server crashed:", e);
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
        writeableServerThread.start();
        readOnlyServerThread.start();

        try {
            /* Block until one of the servers crashes */
            serverMonitor.waitForCrash();

            /* Interrupt server threads to stop application */
            writeableServerThread.interrupt();
            readOnlyServerThread.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); /* Interrupt the main thread */
            logger.error("The server monitor's thread was interrupted: ", e);
        }
    }
}
