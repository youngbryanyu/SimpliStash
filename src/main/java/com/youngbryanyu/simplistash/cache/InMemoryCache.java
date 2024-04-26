package com.youngbryanyu.simplistash.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The in-memory key-value store.
 */
@Component
public class InMemoryCache {
    /**
     * The concurrent hash map containing all key-value pairs in-memory
     */
    private final ConcurrentHashMap<String, String> store;

    /**
     * The constructor for {@link InMemoryCache}.
     */
    @Autowired
    public InMemoryCache() {
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
