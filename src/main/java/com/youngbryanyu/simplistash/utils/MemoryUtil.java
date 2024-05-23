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

    /**
     * Returns the max on-heap memory in bytes.
     * 
     * @return the max on-heap memory in bytes.
     */
    public static long getMaxMemory_onHeap() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns the free on-heap memory available in bytes.
     * 
     * @return the free on-heap memory available in bytes.
     */
    public static long getFreeMemory_onHeap() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Returns the current allocated (total) on-heap memory in bytes.
     * 
     * @return the current allocated (total) on-heap memory in bytes.
     */
    public static long getAllocatedMemory_onHeap() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Returns the current used on-heap memory in bytes.
     * 
     * @return the current used on-heap memory in bytes.
     */
    public static long getUsedMemory_onHeap() {
        return getAllocatedMemory_onHeap() - getFreeMemory_onHeap();
    }
}
