package com.youngbryanyu.simplistash.stash;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

/**
 * Class representing a "stash" which serves as a single table of key-value
 * pairs
 */
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
     * The in-memory cache to store data. Stores data off-heap.
     */
    private final HTreeMap<String, String> cache;

    /**
     * Constructor for a stash.
     */
    public Stash(String name) {
        cache = null;
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
