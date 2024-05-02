package com.youngbryanyu.simplistash;

import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.ReadOnlyServer;
import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.server.ServerMonitor;
import com.youngbryanyu.simplistash.server.WriteableServer;

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

        Server writeableServer = context.getBean(WriteableServer.class);
        Server readOnlyServer = context.getBean(ReadOnlyServer.class);
        ServerMonitor serverMonitor = context.getBean(ServerMonitor.class);
        Logger logger = context.getBean(Logger.class);

        /* Cleanup resources on shutdown */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            context.close();
        }));
        
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
            logger.error("Server monitoring thread interrupted: ", e);
        }
    }

    // // prints the on-heap memory usage
    // public static void printMemoryUsage() {
    //     Runtime runtime = Runtime.getRuntime();

    //     long totalMemory = runtime.totalMemory(); // Total memory currently available to the JVM
    //     long freeMemory = runtime.freeMemory(); // Amount of free memory in the JVM
    //     long usedMemory = totalMemory - freeMemory; // Used memory calculated as totalMemory - freeMemory
    //     long maxMemory = runtime.maxMemory();

    //     // Convert bytes to megabytes
    //     double totalMemoryMB = (double) totalMemory / (1024 * 1024);
    //     double freeMemoryMB = (double) freeMemory / (1024 * 1024);
    //     double usedMemoryMB = (double) usedMemory / (1024 * 1024);
    //     double maxMemoryMB = (double) maxMemory / (1024 * 1024);

    //     System.out.println("Max memory: " + maxMemoryMB);
    //     System.out.println("Total memory (MB): " + totalMemoryMB);
    //     System.out.println("Free memory (MB): " + freeMemoryMB);
    //     System.out.println("Used memory (MB): " + usedMemoryMB);
    // }
}
