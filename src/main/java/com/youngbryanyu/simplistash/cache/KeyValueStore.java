package com.youngbryanyu.simplistash.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.youngbryanyu.simplistash.protocol.ProtocolFormatter;

/**
 * The in-memory key-value store.
 */
public class KeyValueStore {
    /**
     * The concurrent hash map containing all key-value pairs in-memory
     */
    private final ConcurrentHashMap<String, String> store;

    /**
     * The constructor for {@link KeyValueStore}.
     */
    public KeyValueStore() {
        this.store = new ConcurrentHashMap<>();
    }

    /**
     * Gets the value mapped to the input key.
     */
    public String get(String key) {
        return store.get(key);
    }

    /**
     * Sets a new value for the key.
     */
    public void set(String key, String value) {
        store.put(key, value);
    }

    /**
     * Deletes a key and its associated value.
     */
    public void delete(String key) {
        store.remove(key);
    } 
}


// TODO: add TTL
// - TTLed values can be checked on read
// - TTLed values should be cleaned up periodically by some process in the background