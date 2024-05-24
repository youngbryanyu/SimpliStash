package com.youngbryanyu.simplistash.eviction.lru;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.eviction.EvictionTracker;

/**
 * Keeps track of the LRU element in a stash.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LRUTracker implements EvictionTracker {
    /**
     * The linked hash map for LRU tracking. Only 1 thread handles writes so no need
     * to make thread-safe yet.
     */
    private final Set<String> lruKeys;

    /**
     * Constructor for LRU tracker.
     * 
     * @param offHeap Whether or not the stash is off-heap.
     */
    @Autowired
    public LRUTracker() {
        lruKeys = new LinkedHashSet<>();
    }

    /**
     * Add a key.
     * 
     * @param key The key.
     */
    public void add(String key) {
        lruKeys.remove(key); /* Remove the key */
        lruKeys.add(key);
    }

    /**
     * Remove a key.
     * 
     * @param key The key.
     */
    public void remove(String key) {
        lruKeys.remove(key);
    }

    /**
     * Returns whether the LRU tracker has the key.
     * 
     * @param key The key.
     * @return True if the LRU tracker has the key, false otherwise.
     */
    public boolean contains(String key) {
        return lruKeys.contains(key);
    }

    /**
     * Evicts a single key.
     * 
     * @return The key evicted, or null if nothing was evicted.
     */
    public synchronized String evict() {
        Iterator<String> iterator = lruKeys.iterator();
        if (iterator.hasNext()) {
            String lruKey = lruKeys.iterator().next();
            lruKeys.remove(lruKey);
            return lruKey;
        }

        return null;
    }

    /**
     * Removes all keys from the eviction tracker.
     */
    public void clear() {
        lruKeys.clear();
    }
}
