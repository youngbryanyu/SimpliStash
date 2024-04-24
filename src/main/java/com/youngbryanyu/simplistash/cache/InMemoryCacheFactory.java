package com.youngbryanyu.simplistash.cache;

/**
 * Factory class for creating {@link InMemoryCache}.
 */
public class InMemoryCacheFactory {
    /**
     * Private constructor to prevent instantiation.
     */
    private InMemoryCacheFactory() {
    }

    /**
     * Creates an instance of {@link InMemoryCache}.
     */
    public static InMemoryCache createInMemoryCache() {
        return new InMemoryCache();
    }
}
