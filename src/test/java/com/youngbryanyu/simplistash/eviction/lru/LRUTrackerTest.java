package com.youngbryanyu.simplistash.eviction.lru;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.eviction.EvictionTracker;

/**
 * Unit tests for the LRU tracker.
 */
public class LRUTrackerTest {
    /**
     * The LRU tracker under test.
     */
    EvictionTracker evictionTracker;
    
    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        evictionTracker = new LRUTracker();
    }

    /**
     * Test the add method.
     */
    @Test
    public void testAdd() {
        evictionTracker.add("key1");
        assertTrue(evictionTracker.contains("key1"));
    }

    /**
     * Test the contains method.
     */
    @Test
    public void testContains() {
        evictionTracker.add("key1");
        evictionTracker.add("key2");
        assertTrue(evictionTracker.contains("key1"));
        assertTrue(evictionTracker.contains("key2"));
        assertFalse(evictionTracker.contains("key3"));
    }

    /**
     * Test the contains method.
     */
    @Test
    public void testRemove() {
        evictionTracker.add("key1");
        evictionTracker.remove("key1");
        assertFalse(evictionTracker.contains("key1"));
    }

    /**
     * Test the contains method.
     */
    @Test
    public void testEvict() {
        evictionTracker.add("key1");
        evictionTracker.add("key2");
        evictionTracker.add("key3");
        evictionTracker.add("key4");
        evictionTracker.add("key5");
        
        /* 1 and 2 should not be LRU */
        evictionTracker.add("key1");
        evictionTracker.add("key2");

        /* Top 2 LRU should be 3 and 4 */
        assertEquals("key3", evictionTracker.evict());
        assertEquals("key4", evictionTracker.evict());
    }

    /**
     * Test the contains method.
     */
    @Test
    public void testEvict_nothingEvicted() {
        assertNull(evictionTracker.evict());
    }

    /**
     * Test the contains method.
     */
    @Test
    public void testClear() {
        evictionTracker.add("key1");
        evictionTracker.add("key2");
        evictionTracker.add("key3");
        evictionTracker.clear();
        assertEquals(0, evictionTracker.size());
    }
}
