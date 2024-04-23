package com.youngbryanyu.simplistash.cache;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the key value store factory.
 */
public class KeyValueStoreFactoryTest {
    /**
     * Test successfully creating a key value store.
     */
    @Test
    public void testCreateServerHandlerSuccess() {
        KeyValueStore keyValueStore = KeyValueStoreFactory.createKeyValueStore();
        
        assertTrue(keyValueStore instanceof KeyValueStore);
    }
}
