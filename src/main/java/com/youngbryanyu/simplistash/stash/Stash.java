package com.youngbryanyu.simplistash.stash;

import java.util.List;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Class representing a stash which serves as a single table of key-value pairs.
 * The actual key-value pairs are stored off-heap.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Stash {
    /**
     * The max key size allowed in the stash.
     */
    public static final int MAX_KEY_SIZE = 256;
    /**
     * The max value size allowed in the stash.
     */
    public static final int MAX_VALUE_SIZE = 65536;
    /**
     * The max size of a stash's name.
     */
    public static final int MAX_NAME_SIZE = 64;
    /**
     * Name of the primary cache.
     */
    private static final String PRIMARY_CACHE_NAME = "primary";
    /**
     * Error message when attempting to access a closed DB.
     */
    private static final String DB_CLOSED_ERROR = "The specified stash doesn't exist";
    /**
     * A single DB store instance tied to the stash.
     */
    private final DB db;
    /**
     * The primary cache providing O(1) direct access to values by key, and off-heap
     * storage.
     */
    private final HTreeMap<String, String> cache;
    /**
     * Keys in the cache subject to TTL.
     */
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
     * Constructor for the stash.
     * 
     * @param db     The DB instance.
     * @param logger The application logger.
     */
    @Autowired
    public Stash(DB db, TTLTimeWheel ttlTimeWheel, Logger logger, String name) {
        this.db = db;
        this.ttlTimeWheel = ttlTimeWheel;
        this.logger = logger;
        this.name = name;
        cache = createPrimaryCache();
        addShutDownHook();
    }

    /**
     * Creates the primary cache for O(1) access to fields directly.
     */
    private HTreeMap<String, String> createPrimaryCache() {
        return db.hashMap(PRIMARY_CACHE_NAME, Serializer.STRING, Serializer.STRING).create();
    }

    /**
     * Sets a key value pair in the stash. Does not remove existing TTLs on the key.
     * 
     * There cannot be parallel writes operations since there is only one thread
     * which handles writes.
     * 
     * @param key   The unique key.
     * @param value The value to map to the key.
     */
    public void set(String key, String value) {
        cache.put(key, value);
    }

    /**
     * Retrieves a value from the stash matching the key. Returns an error message
     * if the DB is being closed or has already been closed by another concurrent
     * client, since multiple threads can perform read operations. Passively expires
     * the key if it has expired.
     * 
     * @param key The key of the value to get.
     * @return The value matching the key.
     */
    public String get(String key) {
        try {
            if (ttlTimeWheel.isExpired(key)) {
                ttlTimeWheel.remove(key);
                cache.remove(key);

                logger.debug(String.format("Lazy removed key from stash [%s]: %s", name, key));
                return null;
            }

            return cache.get(key);
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
     * Deletes a key from the stash and clears its TTL. Returns the OK response.
     * 
     * There cannot be parallel write operations since there is only one thread
     * which handles writes.
     * 
     * @param key The key to delete.
     */
    public void delete(String key) {
        cache.remove(key);
        ttlTimeWheel.remove(key);
    }

    /**
     * Sets a key value pair in the stash. Updates the key's TTL.
     * 
     * There cannot be parallel writes operations since there is only one thread
     * which handles writes.
     * 
     * @param key   The unique key.
     * @param value The value to map to the key.
     * @param ttl   The ttl of the key.
     */
    public void setWithTTL(String key, String value, long ttl) {
        cache.put(key, value);
        ttlTimeWheel.add(key, ttl);
    }

    /**
     * Drops the stash. Closes its DB.
     * 
     * This is a blocking operation, so nothing can concurrently write to the db
     * while it's being closed since only 1 thread can perform writes. However,
     * since multiple threads can perform reads, the case of reading from a closed
     * DB needs to be handled.
     */
    public void drop() {
        db.close();
    }

    /**
     * Add a shutdown hook to close DB and clean up resources when the application
     * is stopped.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            db.close();
        }));
    }

    /**
     * Gets a batch of expired keys and removes them from the Stash's cache.
     */
    public void expireTTLKeys() {
        List<String> expiredKeys = ttlTimeWheel.expireKeys();
       
        for (String key : expiredKeys) {
            cache.remove(key);
        }

        if (!expiredKeys.isEmpty()) {
            logger.debug(String.format("Expired keys from stash [%s]: %s", name, expiredKeys));        
        }
    }
}
