package com.youngbryanyu.simplistash.utils;

/**
 * Class with memory related util functions.
 */
public class MemoryUtil {
    /* Private constructor to prevent instantiation */
    private MemoryUtil() {
    }

    /*
     * Utility function to print on-heap memory usage.
     */
    public static void printOnHeapMemoryUsage() {
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
