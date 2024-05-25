package com.youngbryanyu.simplistash.stash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.stash.replication.ReplicaHandler;
import com.youngbryanyu.simplistash.stash.replication.ReplicaHandlerFactory;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotWriter;
import com.youngbryanyu.simplistash.utils.FileUtil;
import com.youngbryanyu.simplistash.utils.SerializationUtil;

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
     * The current registered read replica handlers.
     */
    private final List<ReplicaHandler> replicaHandlers = new ArrayList<>();
    /**
     * The replica factory.
     */
    private final ReplicaHandlerFactory replicaFactory;

    /**
     * Constructor for a stash manager.
     * 
     * @param stashFactory The factory used to create the stashes.
     */
    @Autowired
    public StashManager(StashFactory stashFactory, ReplicaHandlerFactory replicaFactory, Logger logger) {
        this.stashFactory = stashFactory;
        this.replicaFactory = replicaFactory;
        this.logger = logger;
        stashes = new ConcurrentHashMap<>();

        /* Recover backups */
        try {
            initializeFromSnapshots();
        } catch (IOException e) {
            logger.warn("Failed to initialize stashes from snapshots: " + e.getMessage());
        }

        /* Create default stash if not recovered from backups */
        if (!stashes.containsKey(DEFAULT_STASH_NAME)) {
            createStash(DEFAULT_STASH_NAME, USE_OFF_HEAP_MEMORY, Stash.DEFAULT_MAX_KEY_COUNT, DEFAULT_ENABLE_BACKUPS);
        }
    }

    /**
     * Creates a new stash with the given name and stores it in the stashes map.
     * Does nothing if the stash name is already taken. Fails if there are already
     * the max number of stashes supported.
     * 
     * @param name            The name of the stash.
     * @param offHeap         Whether or not to use off-heap memory.
     * @param maxKeyCount     The max number of keys allowed.
     * @param enableSnapshots Whether or not to enable periodic snapshots.
     * @return True if the stash was created successfully or already exists, false
     *         otherwise.
     */
    public boolean createStash(String name, boolean offHeap, long maxKeyCount, boolean enableSnapshots) {
        if (stashes.size() >= MAX_NUM_STASHES) {
            return false;
        }

        if (offHeap) {
            stashes.putIfAbsent(name, stashFactory.createOffHeapStash(name, maxKeyCount, enableSnapshots));
        } else {
            stashes.putIfAbsent(name, stashFactory.createOnHeapStash(name, maxKeyCount, enableSnapshots));
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

        /* Get read replica and master info */
        String masterIp = System.getProperty("masterIp");
        String masterPortStr = System.getProperty("masterPort");
        int masterPort = -1;
        if (masterIp != null && !masterIp.isEmpty() && masterPortStr != null && !masterPortStr.isEmpty()) {
            try {
                /* Register node as read replica */
                masterPort = Integer.parseInt(masterPortStr);
            } catch (NumberFormatException e) {
                logger.warn("Failed to register node as a read-replica.");
            }
        }

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

        stats.append("Replication:\n");
        if (masterPort != -1) {
            stats.append(String.format("- Read replica: \t%b\n", true));
            stats.append(String.format("- Master port: \t\t%d\n", masterPort));
            stats.append(String.format("- Master ip: \t\t%s\n", masterIp));
        } else {
            /* Master */
            stats.append(String.format("- Read replica: \t%b\n", false));
            stats.append(String.format("- Read replica count: \t%d\n", replicaHandlers.size()));
            stats.append("\n");
            stats.append("Replica locations:\n");
            for (ReplicaHandler replica : replicaHandlers) {
                stats.append(String.format("- %s/%d\n", replica.getSocket().getInetAddress(),
                        replica.getSocket().getPort()));
            }
        }

        stats.deleteCharAt(stats.length() - 1); // delete extra newline at end

        return stats.toString();
    }

    protected void initializeFromSnapshots() throws IOException {
        logger.info("Initializing stashes from snapshots...");

        /* Prepare directory */
        FileUtil.ensureDirectoryExists(SnapshotWriter.DIR);
        File directory = new File(SnapshotWriter.DIR);
        File[] snapshotFiles = directory.listFiles((dir, name) -> name.endsWith("." + SnapshotWriter.EXTENSION));

        /* No snapshots */
        if (snapshotFiles == null) {
            return;
        }

        /* Loop over files */
        for (File snapshotFile : snapshotFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(snapshotFile))) {
                /* Get metadata in order */
                String stashName = SerializationUtil.decode(reader);
                long maxKeyCount = Long.parseLong(SerializationUtil.decode(reader));
                boolean offHeap = Boolean.parseBoolean(SerializationUtil.decode(reader));

                logger.info(String.format("Initializing stash \"%s\" from snapshot...", stashName));

                /* Create stash */
                Stash stash;
                if (offHeap) {
                    stash = stashFactory.createOffHeapStash(stashName, maxKeyCount, true);
                } else {
                    stash = stashFactory.createOnHeapStash(stashName, maxKeyCount, true);
                }

                /* Populate stash */
                while (true) {
                    /* Parse key */
                    String key = SerializationUtil.decode(reader);
                    if (key == null)
                        break; /* Nothing left to parse */

                    /* Parse value */
                    String value = SerializationUtil.decode(reader);

                    /* Parse ttl */
                    String expirationString = SerializationUtil.decode(reader);
                    long expirationTime = Long.parseLong(expirationString);

                    /* Store the entry */
                    if (expirationTime == -1) {
                        stash.set(key, value);
                    } else {
                        /* TTL is expiration time minus current time */
                        stash.setWithTTL(key, value, expirationTime - System.currentTimeMillis());
                    }
                }

                /* Save stash */
                stashes.put(stashName, stash);
            } catch (IOException e) {
                System.out.println("Failed to initialize a stash from snapshot: " + e.getMessage());
            }
        }
    }

    /**
     * Registers a read replica.
     * 
     * @param port The port.
     * @param ip   The ip.
     */
    public void registerReadReplica(String ip, int port) {
        /* Connect to replica */
        ReplicaHandler replicaHandler = replicaFactory.createReplica(ip, port);
        replicaHandler.connect();
        replicaHandlers.add(replicaHandler);

        logger.info(String.format("Replica registered from: %s/%d", ip, port));
    }

    /**
     * Forwards a command to a read replica.
     * 
     * @param encodedCommand The command to forward, already encoded.
     */
    public void forwardCommandToReadReplicas(String encodedCommand) {
        for (ReplicaHandler replica : replicaHandlers) {
            replica.forwardCommand(encodedCommand);
        }
    }
}
