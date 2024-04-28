package com.youngbryanyu.simplistash.stash;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class representing a "stash" which serves as a single table of key-value
 * pairs
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Stash {
    /**
     * The max key size allowed in the stash
     */
    public static final int MAX_KEY_SIZE = 256;
    /**
     * The max value size allowed in the stash
     */
    public static final int MAX_VALUE_SIZE = 65536;
    /**
     * Name of the primary cache
     */
    private static final String PRIMARY_CACHE_NAME = "primary";
    /**
     * A single DB store instance tied to the stash.
     */
    private DB db;
    /**
     * The primary cache provide O(1) direct access to values by key.
     */
    private HTreeMap<String, String> cache;

    @Autowired
    public Stash(DB db) {
        this.db = db;
        createPrimaryCache();
    }

    /**
     * Creates the primary cache for O(1) access to fields directly.
     */
    private void createPrimaryCache() {
        cache = db.hashMap(PRIMARY_CACHE_NAME, Serializer.STRING, Serializer.STRING).create();
    }

    /**
     * Sets a key value pair in the stash. Overwrites existing pairs.
     * 
     * @param key   The unique key.
     * @param value The value to map to the key.
     */
    public void set(String key, String value) {
        cache.put(key, value);
    }

    /**
     * Retrieves a value from the stash matching the key.
     * 
     * @param key The key of the value to get.
     * @return The value matching the key.
     */
    public String get(String key) {
        return cache.get(key);
    }

    /**
     * Deletes a key from the stash.
     * 
     * @param key The key to delete.
     */
    public void delete(String key) {
        cache.remove(key);
    }
}
