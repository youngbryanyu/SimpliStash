package com.youngbryanyu.simplistash.ttl;

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
    void testExpireKeys() {
        /* Add keys with staggered TTLs */
        for (int i = 0; i < 10; i++) {
            ttlTimeWheel.add("key" + i, i * 5);
        }

       
        try {
            Thread.sleep(1000);
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
}
