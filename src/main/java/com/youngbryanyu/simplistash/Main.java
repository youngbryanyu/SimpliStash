package com.youngbryanyu.simplistash;

import java.util.Random;

import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;

/**
 * The entry point to the application.
 */
public class Main {
    static Random random;
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

    // prints the on-heap memory usage
    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory(); // Total memory currently available to the JVM
        long freeMemory = runtime.freeMemory(); // Amount of free memory in the JVM
        long usedMemory = totalMemory - freeMemory; // Used memory calculated as totalMemory - freeMemory
        long maxMemory = runtime.maxMemory();

        // Convert bytes to megabytes
        double totalMemoryMB = (double) totalMemory / (1024 * 1024);
        double freeMemoryMB = (double) freeMemory / (1024 * 1024);
        double usedMemoryMB = (double) usedMemory / (1024 * 1024);
        double maxMemoryMB = (double) maxMemory / (1024 * 1024);

        System.out.println("Max memory: " + maxMemoryMB);
        System.out.println("Total memory (MB): " + totalMemoryMB);
        System.out.println("Free memory (MB): " + freeMemoryMB);
        System.out.println("Used memory (MB): " + usedMemoryMB);
    }
}
