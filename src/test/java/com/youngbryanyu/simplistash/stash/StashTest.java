package com.youngbryanyu.simplistash.stash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.QueueLong.Node.SERIALIZER;
import org.slf4j.Logger;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

/**
 * Unit tests for the stash class.
 */
class StashTest {
    /**
     * The DB. Cannot mock this.
     */
    private DB db;
    /**
     * The mocked HTreeMap cache. Cannot mock this.
     */
    private HTreeMap<String, String> cache;
    /**
     * The mocked TTL time wheel.
     */
    @Mock
    private TTLTimeWheel mockTTLTimeWheel;
    /**
     * The mocked logger.
     */
    @Mock
    private Logger mockLogger;
    /**
     * The stash under test.
     */
    private Stash stash;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        /* Create real DB and HTreeMap since they cannot be mocked */
        db = DBMaker.memoryDB().make();
        cache = db.hashMap("primary", SERIALIZER.STRING, SERIALIZER.STRING).create();

        stash = new Stash(db, cache, mockTTLTimeWheel, mockLogger, "testStash");
    }

    /**
     * Cleanup after each test.
     */
    @AfterEach
    public void cleanup() {
        db.close();
    }

    /**
     * Test {@link Stash#set(String, String)} when the key has expired previously.
     */
    @Test
    void testSet_expired() {
        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true) /* Return true in set() */
                .thenReturn(false); /* Return false in get() */
        doNothing().when(mockTTLTimeWheel).remove(anyString());

        /* Call method */
        stash.set("key1", "value1");

        /* Test assertions */
        assertEquals("value1", stash.get("key1", false));
        verify(mockTTLTimeWheel, times(1)).remove(anyString());
    }

    /**
     * Test {@link Stash#set(String, String)}.
     */
    @Test
    void testSetAndGet() {
        stash.set("key1", "value1");
        stash.set("key1", "valueX");
        stash.set("key2", "value2");
        assertEquals("valueX", stash.get("key1", false));
        assertEquals("value2", stash.get("key2", false));
    }

    /**
     * Test {@link Stash#get(String, boolean)} when the key has expired.
     */
    @Test
    void testGet_keyExpired() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true);

        /* Call method */
        String result = stash.get("key1", false);

        /* Test assertions */
        assertEquals(null, result);
        verify(mockTTLTimeWheel, atLeast(1)).remove(anyString());
    }

    /**
     * Test {@link Stash#get(String, boolean)} when the key has expired but the
     * client is read only.
     */
    @Test
    void testGet_keyExpired_readOnly() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true);

        /* Call method */
        String result = stash.get("key1", true);

        /* Test assertions */
        assertEquals(null, result);
        verify(mockTTLTimeWheel, never()).remove(anyString());
    }

    /**
     * Test {@link Stash#get(String, boolean)} when a null pointer exception is
     * thrown. We should be mocking the behavior of HTreeMap.get(), but it cannot be
     * mocked so we throw the null pointer elsewhere instead.
     */
    @Test
    void testGet_nullPointerException() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true);
        doThrow(NullPointerException.class).when(mockTTLTimeWheel).remove(anyString());

        /* Call method */
        String result = stash.get("key1", false);

        /* Test assertions */
        assertEquals(ProtocolUtil.buildErrorResponse(Stash.DB_CLOSED_ERROR), result);
    }

    /**
     * Test {@link Stash#get(String, boolean)} when an illegal access error is
     * thrown. We should be mocking the behavior of HTreeMap.get(), but it cannot be
     * mocked so we throw the null pointer elsewhere instead.
     */
    @Test
    void testGet_IllegalAccessError() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true);
        doThrow(IllegalAccessError.class).when(mockTTLTimeWheel).remove(anyString());

        /* Call method */
        String result = stash.get("key1", false);

        /* Test assertions */
        assertEquals(ProtocolUtil.buildErrorResponse(Stash.DB_CLOSED_ERROR), result);
    }

    /**
     * Test {@link Stash#contains(String)}.
     */
    @Test
    void testContains() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(false);

        /* Test assertions */
        assertEquals(true, stash.contains("key1"));
        assertEquals(false, stash.contains("key2"));
    }

    /**
     * Test {@link Stash#delete(String)}.
     */
    @Test
    void testDelete() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(false);

        /* Call method */
        stash.delete("key1");

        /* Test assertions */
        assertEquals(false, stash.contains("key2"));
        verify(mockTTLTimeWheel, times(1)).remove(anyString());
    }

    /**
     * Test {@link Stash#setWithTTL(String, String, long)}.
     */
    @Test
    void testSetWithTTL() {
        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(false);

        /* Call method */
        stash.setWithTTL("key1", "value1", 100L);

        /* Test assertions */
        assertEquals(true, stash.contains("key1"));
        verify(mockTTLTimeWheel, times(1)).add(anyString(), anyLong());
    }

    /**
     * Test {@link Stash#updateTTL(String, long)}.
     */
    @Test
    void testUpdateTTL() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Call method */
        boolean updatedTTL = stash.updateTTL("key1", 100L);

        /* Test assertions */
        assertEquals(true, updatedTTL);
        verify(mockTTLTimeWheel, times(1)).add(anyString(), anyLong());
    }

    /**
     * Test {@link Stash#updateTTL(String, long)} when the key doesn't exist.
     */
    @Test
    void testUpdateTTL_keyDoesntExist() {
       /* Call method */
       boolean updatedTTL = stash.updateTTL("key1", 100L);

       /* Test assertions */
       assertEquals(false, updatedTTL);
       verify(mockTTLTimeWheel, never()).add(anyString(), anyLong());
    }

    /**
     * Test {@link Stash#drop()}.
     */
    @Test
    void testDrop() {
       /* Call method */
       stash.drop();

       /* Test assertions */
       assertEquals(ProtocolUtil.buildErrorResponse(Stash.DB_CLOSED_ERROR), stash.get("key1", false));
    }

    /**
     * Test {@link Stash#updateTTL(String, long)}.
     */
    @Test
    void testExpireTTLKeys() {
        /* Setup */
        when(mockTTLTimeWheel.expireKeys()).thenReturn(List.of("key1", "key2"));
        
        /* Call method */
        stash.expireTTLKeys();

        /* Test assertions */
        verify(mockTTLTimeWheel, times(1)).expireKeys();
    }
}
