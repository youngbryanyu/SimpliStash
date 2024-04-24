package com.youngbryanyu.simplistash.cache;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the key value store factory.
 */
public class InMemoryCacheFactoryTest {
    /**
     * Test successfully creating a key value store.
     */
    @Test
    public void testCreateServerHandlerSuccess() {
        InMemoryCache cache = InMemoryCacheFactory.createInMemoryCache();

        assertTrue(cache instanceof InMemoryCache);
    }
}
