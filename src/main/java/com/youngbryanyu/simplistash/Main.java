package com.youngbryanyu.simplistash;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

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

    /*
     * HASHMAP:
     * Test 1:
     * - Overhead writing 1 million entries (STRING, LONG). Key is 256+ bytes, Val
     * is
     * 8 bytes:
     * 
     * Hashmap: used 408 MB, took .571 s
     * HTreeMap: used 688 MB, took 3.683 s
     * Eclipse collections: used 365 MB, took .538 s
     * Trove: 391 MB, .586 s
     * Fastutil 381 MB, .6 s
     * HPPC: 371 MB, .669 s
     * Caffeine: 416 MB, .601
     * 
     * Test 2:
     * - Write 1 million, then clear/write 1 million 9 times:
     * Hashmap: used 628 MB, took 4.3 s
     * HTreeMap: used 1763 MB, took 57.83 s
     * Eclipse collections: used 608 MB, took 4.542 s
     * Trove: used 382 MB, 4.725 s
     * fastutil: 587 MB, 4.727 s
     * HPPC: 640 MB, 4.712 s
     * Caffeine: 416 MB, 4.893
     * 
     * TREEMAP tests:
     * test1:
     * Treemap: 398 MB, .236s
     * BTreemap: 1091 MB, 40s
     * eclipse: 395 MB, .278
     * trove: N/A
     * fastutil (avl): 381 MB, .276
     * fastutil (rb): 374, .34
     * hppc: none
     * 
     * test 2:
     * Treemap: 398 MB, .236s
     * BTreemap: 1091 MB, 40s
     * eclipse: 395 MB, .278
     * trove: N/A
     * fastutil (avl): 381 MB, .276
     * fastutil (rb): 1086, .2.71
     * hppc: none
     * 
     * List<Map<Long, List<String>>>, 4320 length, insert 1000 items
     * - 4320 is num buckets for 10 second ticks for a month
     * native: 349 MB, 1.138 s
     * fastutil: 333 MB, 1.024
     * eclipse: 514 MB, .944
     * 
     * z = 1000
     * eclipse: 786 MB, 7.5
     * fastutil: 504 MB, 8.064
     * native: 218 MB, 10,072
     */

    /*
     * Utility function to print memory usage
     */
    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        /* Convert bytes to megabytes */
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
