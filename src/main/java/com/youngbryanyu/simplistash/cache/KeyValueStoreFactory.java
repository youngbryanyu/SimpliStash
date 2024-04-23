package com.youngbryanyu.simplistash.cache;

/**
 * Factory class for creating {@link KeyValueStore}.
 */
public class KeyValueStoreFactory {
    /**
     * Private constructor to prevent instantiation.
     */
    private KeyValueStoreFactory() {
    }

    /**
     * Creates an instance of {@link KeyValueStore}.
     */
    public static KeyValueStore createKeyValueStore() {
        return new KeyValueStore();
    }
}
