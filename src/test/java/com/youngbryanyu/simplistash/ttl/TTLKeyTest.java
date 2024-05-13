package com.youngbryanyu.simplistash.ttl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TTL compound key.
 */
public class TTLKeyTest {
    /**
     * Test comparing keys with different expiration times.
     */
    @Test
    public void testCompareTo_DifferentExpirationTimes() {
        TTLKey key1 = new TTLKey("key1", 1000L);
        TTLKey key2 = new TTLKey("key2", 2000L);

        assertTrue(key1.compareTo(key2) < 0);
        assertTrue(key2.compareTo(key1) > 0);
    }

    /**
     * Test comparing keys with same expiration times.
     */
    @Test
    public void testCompareTo_SameExpirationTime() {
        TTLKey key1 = new TTLKey("key1", 1000L);
        TTLKey key2 = new TTLKey("key2", 1000L);

        assertTrue(key1.compareTo(key2) < 0);
        assertTrue(key2.compareTo(key1) > 0);
    }

    /**
     * Test comparing keys with same expiration times and same key values.
     */
    @Test
    public void testCompareTo_SameKeyAndExpiration() {
        TTLKey key1 = new TTLKey("key1", 1000L);
        TTLKey key2 = new TTLKey("key1", 1000L);

        assertEquals(0, key1.compareTo(key2));
    }

    /**
     * Test getting the key from the compound key.
     */
    @Test
    public void testGetKey() {
        TTLKey key = new TTLKey("key1", 1000L);
        assertEquals("key1", key.getKey());
    }

    /**
     * Test getting the expiration time from the compound key.
     */
    @Test
    public void testGetExpirationTime() {
        TTLKey key = new TTLKey("key1", 1000L);
        assertEquals(1000L, key.getExpirationTime());
    }

    /**
     * Test the to string method.
     */
    @Test
    public void testToString() {
        TTLKey key = new TTLKey("key1", 1000L);
        assertEquals("key=key1,exp=1000", key.toString());
    }
}
