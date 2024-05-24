package com.youngbryanyu.simplistash.stash;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A class to manage all the stashes.
 */
@Component
public class StashManager {
    /**
     * The name of the default stash used when none is specified by the client.
     */
    public static final String DEFAULT_STASH_NAME = "default";
    /**
     * The maximum number of stashes that can be created.
     */
    public static final int MAX_NUM_STASHES = 100;
    /**
     * Whether or not to enable backups by default.
     */
    public static final boolean DEFAULT_ENABLE_BACKUPS = false;
    /**
     * The factory used to create new stash instances.
     */
    private final StashFactory stashFactory;
    /**
     * Mapping of each stash's name to its instance.
     */
    private final Map<String, Stash> stashes;
    /**
     * The application logger.
     */
    private final Logger logger;
    /**
     * Whether or not to default to off-heap memory.
     */
    public static final boolean USE_OFF_HEAP_MEMORY = true;

    /**
     * Constructor for a stash manager.
     * 
     * @param stashFactory The factory used to create the stashes.
     */
    @Autowired
    public StashManager(StashFactory stashFactory, Logger logger) {
        this.stashFactory = stashFactory;
        this.logger = logger;
        stashes = new ConcurrentHashMap<>();

        /* Create default stash */
        createStash(DEFAULT_STASH_NAME, USE_OFF_HEAP_MEMORY, Stash.DEFAULT_MAX_KEY_COUNT, DEFAULT_ENABLE_BACKUPS);
    }

    /**
     * Creates a new stash with the given name and stores it in the stashes map.
     * Does nothing if the stash name is already taken. Fails if there are already
     * the max number of stashes supported.
     * 
     * @param name        The name of the stash.
     * @param offHeap     Whether or not to use off-heap memory.
     * @param maxKeyCount The max number of keys allowed.
     * @param enableBackups Whether or not to enable periodic backups.
     * @return True if the stash was created successfully or already exists, false
     *         otherwise.
     */
    public boolean createStash(String name, boolean offHeap, long maxKeyCount, boolean enableBackups) {
        if (stashes.size() >= MAX_NUM_STASHES) {
            return false;
        }

        if (offHeap) {
            stashes.putIfAbsent(name, stashFactory.createOffHeapStash(name, maxKeyCount, enableBackups));
        } else {
            stashes.putIfAbsent(name, stashFactory.createOnHeapStash(name, maxKeyCount, enableBackups));
        }

        return true;
    }

    /**
     * Gets a stash that matches the given name.
     * 
     * @param name The name of the stash.
     * @return Returns the stash corresponding to the name.
     */
    public Stash getStash(String name) {
        return stashes.get(name);
    }

    /**
     * Returns whether or not a stash with the given name exists.
     * 
     * @param name The stash name.
     * @return True if a stash with the given name exists, false otherwise.
     */
    public boolean containsStash(String name) {
        return stashes.containsKey(name);
    }

    /**
     * Drops a stash. Does nothing if the stash has already been dropped.
     * 
     * @param name The name of the stash to delete.
     */
    public void dropStash(String name) {
        Stash stash = getStash(name);

        if (stash != null) {
            stashes.remove(name);
            stash.drop();
        }
    }

    /**
     * Loops through each stash's TTL timer wheel and expires any expired keys.
     */
    public void expireTTLKeys() {
        for (Stash stash : stashes.values()) {
            stash.expireTTLKeys();
        }
    }

    /**
     * Returns the number of active stashes.
     * 
     * @return The number of active stashes.
     */
    public int getNumStashes() {
        return stashes.size();
    }

    /**
     * Get stats about the overall system:
     * - memory stats
     * - disk stats
     * - stash stats
     * 
     * @return A string containing overal stats info about the system.
     */
    public String getStats() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        File file = new File("/");

        /* Get on-heap memory info */
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long heapUsed = heapMemoryUsage.getUsed();
        long heapMax = heapMemoryUsage.getMax(); /* -1 if undefined */
        long heapAvailable = (heapMax == -1) ? -1 : (heapMax - heapUsed);
        long heapCommitted = heapMemoryUsage.getCommitted();

        /* Get off-heap memory info */
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        long nonHeapUsed = nonHeapMemoryUsage.getUsed();
        long nonHeapMax = nonHeapMemoryUsage.getMax(); /* -1 if undefined */
        long nonHeapAvailable = (nonHeapMax == -1) ? -1 : (nonHeapMax - nonHeapUsed);

        /* Get disk info */
        long freeDiskSpace = file.getFreeSpace();
        long totalDiskSpace = file.getTotalSpace();
        long usableDiskSpace = file.getUsableSpace();

        /* Build stats response */
        StringBuilder stats = new StringBuilder();

        stats.append("Heap memory stats (bytes):\n");
        stats.append(String.format("- Used: \t\t%d\n", heapUsed));
        stats.append(String.format("- Max: \t\t\t%d\n", heapMax));
        stats.append(String.format("- Available: \t\t%d\n", heapAvailable));
        stats.append(String.format("- Committed: \t\t%d\n", heapCommitted));
        stats.append("\n");

        stats.append("Non-heap memory stats (bytes):\n");
        stats.append(String.format("- Used: \t\t%d\n", nonHeapUsed));
        stats.append(String.format("- Max: \t\t\t%d\n", nonHeapMax));
        stats.append(String.format("- Available: \t\t%d\n", nonHeapAvailable));
        stats.append("\n");

        stats.append("Disk stats (bytes):\n");
        stats.append(String.format("- Free space: \t\t%d\n", freeDiskSpace));
        stats.append(String.format("- Total space: \t\t%d\n", totalDiskSpace));
        stats.append(String.format("- Usable space: \t%d\n", usableDiskSpace));
        stats.append("\n");

        stats.append("General stash stats:\n");
        stats.append(String.format("- Number of stashes: \t%d\n", stashes.size()));
        stats.append(String.format("- Stash limit: \t\t%d\n", StashManager.MAX_NUM_STASHES));
        stats.append("\n");

        stats.append("Specific stash stats:\n\n");
        for (String name : stashes.keySet()) {
            Stash stash = stashes.get(name);
            stats.append(String.format("Name: %s\n", name));
            stats.append(stash.getInfo());
            stats.append("\n");
        }

        stats.deleteCharAt(stats.length() - 1); // delete extra newline at end

        return stats.toString();
    }
}
