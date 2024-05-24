package com.youngbryanyu.simplistash.stash;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.eviction.EvictionTracker;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotManager;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotWriterFactory;
import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

/**
 * A stash which serves as a single table of key-value pairs, storing values
 * on-heap.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OnHeapStash implements Stash {
    /**
     * The primary cache providing O(1) direct access to values by key.
     */
    private final Map<String, String> cache;
    /**
     * Time wheel structure used to actively expire TTLed keys.
     */
    private final TTLTimeWheel ttlTimeWheel;
    /**
     * The application logger.
     */
    private final Logger logger;
    /**
     * The name of the Stash.
     */
    private final String name;
    /**
     * The key eviction tracker.
     */
    private final EvictionTracker evictionTracker;
    /**
     * The max number of keys allowed in the stash.
     */
    private final long maxKeyCount;
    /**
     * Whether to enable periodic snapshots.
     */
    private final boolean enableSnapshots;
    /**
     * The snap shot manager.
     */
    private final SnapshotManager snapshotManager;
    /**
     * The snapshot writer factory.
     */
    private final SnapshotWriterFactory snapshotWriterFactory;

    /**
     * Constructor for the stash.
     * 
     * @param cache        The ConcurrentHashMap cache.
     * @param ttlTimeWheel The ttl timer wheel.
     * @param logger       The application logger.
     * @param name         The stash's name.
     */
    @Autowired
    public OnHeapStash(
            Map<String, String> cache,
            TTLTimeWheel ttlTimeWheel,
            Logger logger,
            EvictionTracker evictionTracker,
            String name,
            long maxKeyCount,
            boolean enableSnapshots,
            SnapshotWriterFactory snapshotWriterFactory) throws IOException {
        this.cache = cache;
        this.ttlTimeWheel = ttlTimeWheel;
        this.logger = logger;
        this.evictionTracker = evictionTracker;
        this.name = name;
        this.maxKeyCount = maxKeyCount;
        this.enableSnapshots = enableSnapshots;

        this.snapshotWriterFactory = snapshotWriterFactory;

        snapshotManager = new SnapshotManager(name, maxKeyCount, false, cache, ttlTimeWheel,
                snapshotWriterFactory.createSnapshotWriter(name, enableSnapshots), logger);

        /* Start snapshot manager thread if enabled */
        if (enableSnapshots) {
            snapshotManager.start();
        }

        addShutDownHook();
    }

    /**
     * Sets a key value pair in the stash. Does not change existing TTL on the key.
     * 
     * @param key   The unique key.
     * @param value The value to map to the key.
     */
    public void set(String key, String value) {
        /* Remove TTL metadata in case key previously expired */
        if (ttlTimeWheel.isExpired(key)) {
            ttlTimeWheel.remove(key);
        }

        cache.put(key, value);
        evictionTracker.add(key);

        evictKeys(); /* Evict keys if over memory limit */

        if (enableSnapshots) {
            snapshotManager.markBackupNeeded(); /* Set backup needed */
        }
    }

    /**
     * Retrieves a value from the stash matching the key. Returns an error message
     * if the DB is being closed or has already been closed by another concurrent
     * client. Lazy expires the key if it has expired and the client isn't
     * read-only.
     * 
     * @param key      The key of the value to get.
     * @param readOnly Whether or not the client is read-only.
     * @return The value matching the key.
     */
    public String get(String key, boolean readOnly) {
        try {
            /* Get value if key isn't expired */
            if (!ttlTimeWheel.isExpired(key)) {
                evictionTracker.add(key);
                return cache.get(key);
            }

            /* Lazy expire if not read-only */
            if (!readOnly) {
                cache.remove(key);
                ttlTimeWheel.remove(key);
                evictionTracker.remove(key);

                logger.debug(String.format("Lazy removed key from stash \"%s\": %s", name, key));
            }

            /* Return null since key expired */
            return null;
        } catch (NullPointerException e) {
            /*
             * The below exception can be thrown when the DB is being closed by another
             * thread:
             * 
             * java.lang.NullPointerException: Cannot read the array length because "slices"
             * is null
             */
            logger.debug("Stash get failed, stash doesn't exist (NullPointerException)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        } catch (IllegalAccessError e) {
            /*
             * The below exception can be thrown when the DB has been closed by another
             * thread:
             * 
             * java.lang.IllegalAccessError: Store was closed
             */
            logger.debug("Stash get failed, stash doesn't exist (IllegalAccessError)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        }
    }

    /**
     * Returns whether or not the stash contains the given key.
     * 
     * @param key The key.
     * @return True if the stash contains the key, false otherwise.
     */
    public boolean contains(String key, boolean readOnly) {
        return get(key, readOnly) != null;
    }

    /**
     * Deletes a key from the stash and clears its TTL.
     * 
     * @param key The key to delete.
     */
    public void delete(String key) {
        cache.remove(key);
        ttlTimeWheel.remove(key);
        evictionTracker.remove(key);

        if (enableSnapshots) {
            snapshotManager.markBackupNeeded(); /* Set backup needed */
        }
    }

    /**
     * Sets a key value pair in the stash. Updates the key's TTL.
     * 
     * @param key   The key.
     * @param value The value to map to the key.
     * @param ttl   The ttl of the key.
     */
    public void setWithTTL(String key, String value, long ttl) {
        cache.put(key, value);
        ttlTimeWheel.add(key, ttl);
        evictionTracker.add(key);

        evictKeys(); /* Evict keys if over memory limit */

        if (enableSnapshots) {
            snapshotManager.markBackupNeeded(); /* Set backup needed */
        }
    }

    /**
     * Updates the TTL of a given key. Returns whether or not the key exists and the
     * TTL operation succeeded.
     * 
     * @param key The key.
     * @param ttl The key's new TTL.
     * @return True if the TTL was updated, false if the key doesn't exist.
     */
    public boolean updateTTL(String key, long ttl) {
        if (!contains(key, false)) {
            return false;
        }

        ttlTimeWheel.add(key, ttl);
        evictionTracker.add(key);

        if (enableSnapshots) {
            snapshotManager.markBackupNeeded(); /* Set backup needed */
        }

        return true;
    }

    /**
     * Drops the stash. Closes its DB.
     */
    public void drop() {
        if (enableSnapshots) {
            try {
                snapshotManager.close(); /* Set backup needed */
            } catch (IOException e) {
                logger.debug("Failed to close the snapshot manager: " + e.getMessage());
            }
           
        }

        cache.clear();
    }

    /**
     * Add a shutdown hook to close DB and clean up resources when the application
     * is stopped.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                snapshotManager.close(); /* Set backup needed */
            } catch (IOException e) {
                logger.debug("Failed to close the snapshot manager: " + e.getMessage());
            }

            cache.clear();
        }));
    }

    /**
     * Gets a batch of expired keys and removes them from the stash's cache.
     */
    public void expireTTLKeys() {
        List<String> expiredKeys = ttlTimeWheel.expireKeys();
        for (String key : expiredKeys) {
            cache.remove(key);
            evictionTracker.remove(key);
        }

        if (!expiredKeys.isEmpty()) {
            logger.debug(String.format("Expired keys from stash \"%s\": %s", name, expiredKeys));
        }
    }

    /**
     * Returns information about the stash.
     * 
     * @return Info about the stash.
     */
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("- Number of keys: \t%d\n", cache.size()));
        sb.append(String.format("- Max keys allowed: \t%s\n", maxKeyCount));
        sb.append("- Off-heap: \t\tfalse\n");
        sb.append(String.format("- Snapshots enabled: \t%b\n", enableSnapshots));
        return sb.toString();
    }

    /**
     * Evicts keys until the number of keys is below this limit.
     */
    public void evictKeys() {
        while (cache.size() > maxKeyCount) {
            String evictedKey = evictionTracker.evict();

            /* No more keys to evict */
            if (evictedKey == null) {
                return;
            }

            cache.remove(evictedKey);
            ttlTimeWheel.remove(evictedKey);
            logger.debug(String.format("Evicted key from stash \"%s\": %s", name, evictedKey));
        }
    }

    /**
     * Clears all keys from the stash.
     */
    public void clear() {
        cache.clear();
        ttlTimeWheel.clear();
        evictionTracker.clear();
    }

    /**
     * Returns the stash's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the stash's max key count.
     * 
     * @return The stash's max key count.
     */
    public long getMaxKeyCount() {
        return maxKeyCount;
    }

    /**
     * Returns whether or not backups are enabled.
     * 
     * @return True if backups are enabled, false otherwise.
     */
    public boolean isBackupEnabled() {
        return enableSnapshots;
    }

    /**
     * Returns the expiration time assoicated with the key. Returns -1 if there is
     * no TTL associated.
     * 
     * @return Returns the expiration time assoicated with the key.
     */
    public long getExpirationTime(String key) {
        return ttlTimeWheel.getExpirationTime(key);
    }

    /**
     * Returns a map of all entries inside the stash.
     * 
     * @return A map of all entries inside the stash.
     */
    public Map<String, String> getAllEntries() {
        return cache;
    }
}

// TODO: implement snapshots like off heap stash