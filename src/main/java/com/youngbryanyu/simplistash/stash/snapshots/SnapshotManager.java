package com.youngbryanyu.simplistash.stash.snapshots;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

/**
 * The snapshot manager.
 */
public class SnapshotManager {
    /**
     * The scheduler that periodically performs the snapshot.
     */
    private final ScheduledExecutorService scheduler;
    /**
     * The snapshot writer.
     */
    private final SnapshotWriter snapshotWriter;
    /**
     * The cache.
     */
    private final Map<String, String> cache;
    /**
     * The TTL time wheel.
     */
    private final TTLTimeWheel ttlTimeWheel;
    /**
     * Whether or not a backup is currently needed since a write was recently
     * performed.
     */
    private boolean backupNeeded;
    /**
     * The stash name.
     */
    private String name;
    /**
     * The max key count.
     */
    private long maxKeyCount;
    /**
     * The logger.
     */
    private Logger logger;

    /**
     * The constructor
     * 
     * @param name           The stash name.
     * @param maxKeyCount    The max key count.
     * @param cache          The cache map.
     * @param ttlTimeWheel   The TTL data structure
     * @param snapshotWriter The snap shot writer.
     */
    public SnapshotManager(String name, long maxKeyCount, Map<String, String> cache, TTLTimeWheel ttlTimeWheel,
            SnapshotWriter snapshotWriter, Logger logger) {
        this.name = name;
        this.maxKeyCount = maxKeyCount;
        this.cache = cache;
        this.ttlTimeWheel = ttlTimeWheel;
        this.snapshotWriter = snapshotWriter;
        this.logger = logger;

        backupNeeded = false;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Starts the scheduler to regularly take snapshots
     */
    public void start() {
        scheduler.scheduleWithFixedDelay(this::takeSnapshot, 0, Stash.BACKUP_DELAY_S, TimeUnit.SECONDS);
    }

    /**
     * Stops the scheduler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }

    /**
     * Loops over all entries in the cache and backs them up to disk.
     */
    private void takeSnapshot() {
        try {
            if (backupNeeded) {
                logger.debug("Snapshot started for stash: " + name);

                /* Open the writer */
                snapshotWriter.open(); 

                /* Write metadata first */
                snapshotWriter.writeMetadata(name, maxKeyCount);

                /* Write each entry with ttl */
                for (Map.Entry<String, String> entry : cache.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    long expirationTime = ttlTimeWheel.getExpirationTime(key);
                    snapshotWriter.writeEntry(key, value, expirationTime);
                }

                /* Commit and writer */
                snapshotWriter.commit();
                snapshotWriter.close(); 
                backupNeeded = false;

                logger.debug("Snapshot finished for stash: " + name);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks the flag indicated that a backup is needed since a write was performed
     * recently.
     */
    public void markBackupNeeded() {
        backupNeeded = true;
    }

    /**
     * Closes the snapshot writer.
     * @throws IOException If an IO exception occurs.
     */
    public void close() throws IOException {
        snapshotWriter.close();
    }
}
