package com.youngbryanyu.simplistash.ttl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TTL time wheel.
 */
public class TTLTimeWheelTest {
    /**
     * The TTL time wheel under test.
     */
    private TTLTimeWheel ttlTimeWheel;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        ttlTimeWheel = new TTLTimeWheel();
    }

    /**
     * Test {@link TTLTimeWheel#add(String, long)} and
     * {@link TTLTimeWheel#isExpired(String)}.
     */
    @Test
    public void testAddAndIsExpired() {
        /* Add key to TTL wheel */
        String key1 = "key1";
        String key2 = "key2";
        long ttl = 1000;
        ttlTimeWheel.add(key1, ttl);
        ttlTimeWheel.add(key2, ttl);

        /* Key shouldn't be expired right away */
        assertFalse(ttlTimeWheel.isExpired(key1));
        assertFalse(ttlTimeWheel.isExpired(key2));

        /* Sleep until the key expires */
        try {
            Thread.sleep(ttl + 100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        /* Key shouldbe expired now */
        assertTrue(ttlTimeWheel.isExpired(key1));
        assertTrue(ttlTimeWheel.isExpired(key2));
    }

    /**
     * Test {@link TTLTimeWheel#remove(String)}.
     */
    @Test
    public void testRemove() {
        /* Add and remove a key */
        String key = "key1";
        long ttl = 1000;
        ttlTimeWheel.add(key, ttl);
        ttlTimeWheel.remove(key);

        /* A key shouldn't be expired if it doesn't exist */
        assertFalse(ttlTimeWheel.isExpired(key));
    }

    /**
     * Test {@link TTLTimeWheel#remove(String)} when removing a key that doesn't exist.
     */
    @Test
    public void testRemove_keyDoesntExist() {
        /* Add and remove a key */
        String key1 = "key1";
        String key2 = "key2";
        long ttl = TTLTimeWheel.BUCKET_WINDOW_SIZE + 100;
        ttlTimeWheel.add(key2, ttl);
        ttlTimeWheel.remove(key1);

        /* A key shouldn't be expired if it doesn't exist */
        assertFalse(ttlTimeWheel.isExpired(key1));
        assertFalse(ttlTimeWheel.isExpired(key2));
    }

    /**
     * Test {@link TTLTimeWheel#expireKeys()}.
     */
    @Test
    public void testExpireKeys() {
        /* Add keys with staggered TTLs */
        for (int i = 0; i < 10; i++) {
            ttlTimeWheel.add("key" + i, 0);
        }

        /* Sleep to let TTL times reach */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        /* Expire some keys */
        List<String> expiredKeys = ttlTimeWheel.expireKeys();        

        /* Check expired keys */
        for (int i = 0; i < 5; i++) {
            assertTrue(expiredKeys.contains("key" + i));
        }
    }

    /**
     * Test {@link TTLTimeWheel#expireKeys()} when the expire limit is reached in a single call.
     */
    @Test
    public void testExpireKeys_reachedExpireLimit() {
        /* Add keys with staggered TTLs */
        for (int i = 0; i < TTLTimeWheel.MAX_EXPIRE_LIMIT + 5; i++) {
            ttlTimeWheel.add("key" + i, 0);
        }

        /* Sleep to let TTL times reach */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        /* Expire some keys */
        List<String> expiredKeys = ttlTimeWheel.expireKeys();        

        /* Check expired keys */
        assertTrue(expiredKeys.size() == TTLTimeWheel.MAX_EXPIRE_LIMIT);
    }

    /**
     * Test {@link TTLTimeWheel#expireKeys()} when there's multiple buckets used.
     */
    @Test
    public void testExpireKeys_multipleBuckets() {
        /* Add keys with staggered TTLs */
        ttlTimeWheel.add("key1", 0);
        ttlTimeWheel.add("key2", TTLTimeWheel.BUCKET_WINDOW_SIZE + 100); 

        /* Sleep to let TTL times reach */
        try {
            Thread.sleep(100); /* key2 won't expire after 1000 ms */
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        /* Expire some keys */
        List<String> expiredKeys = ttlTimeWheel.expireKeys();        

        /* Check expired keys */
        assertTrue(expiredKeys.size() == 1);
    }

    /**
     * Test {@link TTLTimeWheel#size()}.
     */
    @Test
    public void testSize() {
        ttlTimeWheel.add("key1", 1000);
        ttlTimeWheel.add("key2", 1000);
        assertEquals(2, ttlTimeWheel.size());
    }

    /**
     * Test {@link TTLTimeWheel#clear()}.
     */
    @Test
    public void testClear() {
        ttlTimeWheel.add("key1", 1000);
        ttlTimeWheel.add("key2", 1000);
        ttlTimeWheel.clear();
        assertEquals(0, ttlTimeWheel.size());
    }
}
