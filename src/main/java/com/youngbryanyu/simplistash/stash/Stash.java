package com.youngbryanyu.simplistash.stash;

import java.io.IOException;

/**
 * The stash interface.
 */
public interface Stash {
    /**
     * The max key length allowed in the stash.
     */
    public static final int MAX_KEY_LENGTH = 256;
    /**
     * The max value length allowed in the stash.
     */
    public static final int MAX_VALUE_LENGTH = 65536;
    /**
     * The max allowed length of a stash's name.
     */
    public static final int MAX_NAME_LENGTH = 64;
    /**
     * Error message when attempting to access a closed DB.
     */
    public static final String DB_CLOSED_ERROR = "The specified stash doesn't exist.";
    /**
     * The max number of keys allowed in the cache before eviction.
     */
    public static final long DEFAULT_MAX_KEY_COUNT = 1_000_000;
    /**
     * The delay between backups in seconds.
     */
    public static final int SNAPSHOT_DELAY_S = 60;

    /**
     * Set a key to a value.
     * 
     * @param key   The key.
     * @param value The value.
     */
    public void set(String key, String value);

    /**
     * Get a key's value.
     * 
     * @param key      The key.
     * @param readOnly Whether or not the client is read only. Will not lazy-expire
     *                 keys if read-only.
     * @return The key's value.
     */
    public String get(String key, boolean readOnly);

    /**
     * Returns whether the stash contains a key.
     * 
     * @param key      The key.
     * @param readOnly Whether or not the client is read only. Will not lazy-expire
     *                 keys if read-only.
     * @return The key's value.
     */
    public boolean contains(String key, boolean readOnly);

    /**
     * Deletes a key.
     * 
     * @param key The key.
     */
    public void delete(String key);

    /**
     * Sets a key to a value with TTL.
     * 
     * @param key   The key.
     * @param value The value.
     * @param ttl   The TTL in milliseconds.
     */
    public void setWithTTL(String key, String value, long ttl);

    /**
     * Updates a key's ttl.
     * 
     * @param key The key.
     * @param ttl The new TTL from the current time.
     * @return True if the TTL update was successful, false otherwise.
     */
    public boolean updateTTL(String key, long ttl);

    /**
     * Drops the stash.
     * @throws IOException 
     */
    public void drop();

    /**
     * Expires a batch of TTLed keys.
     */
    public void expireTTLKeys();

    /**
     * Returns info about the stash.
     * 
     * @return info about the stash.
     */
    public String getInfo();

    /**
     * Evicts keys when not enough memory is left.
     */
    public void evictKeys();

    /**
     * Clears an entire stash.
     */
    public void clear();
}
