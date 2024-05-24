package com.youngbryanyu.simplistash.eviction;

/**
 * Interface for eviction tracker
 */
public interface EvictionTracker {
    /**
     * Add a key.
     * 
     * @param key The key.
     */
    public void add(String key);

    /**
     * Remove a key.
     * 
     * @param key The key.
     */
    public void remove(String key);

    /**
     * Returns whether the LRU tracker has the key.
     * 
     * @param key The key.
     * @return True if the LRU tracker has the key, false otherwise.
     */
    public boolean contains(String key);

    /**
     * Evicts a single key.
     * 
     * @return The key evicted, or null if nothing was evicted.
     */
    public String evict();

    /**
     * Removes all keys from the eviction tracker.
     */
    public void clear();
}
