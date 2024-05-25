package com.youngbryanyu.simplistash.stash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
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

import com.youngbryanyu.simplistash.eviction.EvictionTracker;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotWriter;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotWriterFactory;
import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

/**
 * Unit tests for the off-heap stash class.
 */
class OffHeapStashTest {
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
     * The mocked LRU tracker
     */
    @Mock
    private EvictionTracker mockEvictionTracker;
    /**
     * The mocked snapshot writer factory.
     */
    @Mock
    private SnapshotWriterFactory mockSnapshotWriterFactory;
    /**
     * The mock snapshot writer.
     */
    @Mock
    private SnapshotWriter mockSnapshotWriter;
    /**
     * The stash under test.
     */
    private OffHeapStash stash;

    /**
     * Setup before each test.
     * @throws IOException 
     */
    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        /* Create real DB and HTreeMap since they cannot be mocked */
        db = DBMaker.memoryDB().make();
        cache = db.hashMap("primary", SERIALIZER.STRING, SERIALIZER.STRING).create();

        /* Create snapshot writer  */
        when(mockSnapshotWriterFactory.createSnapshotWriter(anyString(), anyBoolean())).thenReturn(mockSnapshotWriter);

        /* Enable snapshots by default */
        stash = new OffHeapStash(db, cache, mockTTLTimeWheel, mockLogger, mockEvictionTracker, "testStash",
                Stash.DEFAULT_MAX_KEY_COUNT, true, mockSnapshotWriterFactory);
    }

    /**
     * Cleanup after each test.
     */
    @AfterEach
    public void cleanup() {
        db.close();
    }

    /**
     * Test {@link OffHeapStash#set(String, String)} when the key has expired
     * previously.
     */
    @Test
    public void testSet_expired() {
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
     * Test {@link OffHeapStash#set(String, String)}.
     */
    @Test
    public void testSetAndGet() {
        stash.set("key1", "value1");
        stash.set("key1", "valueX");
        stash.set("key2", "value2");
        assertEquals("valueX", stash.get("key1", false));
        assertEquals("value2", stash.get("key2", false));
    }

     /**
     * Test {@link OffHeapStash#set(String, String)} with snapshots enabled.
     */
    @Test
    public void testSet_enableSnapshots() {
        stash.set("key1", "value1");
    }

    /**
     * Test {@link OffHeapStash#get(String, boolean)} when the key has expired.
     */
    @Test
    public void testGet_keyExpired() {
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
     * Test {@link OffHeapStash#get(String, boolean)} when the key has expired but
     * the
     * client is read only.
     */
    @Test
    public void testGet_keyExpired_readOnly() {
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
     * Test {@link OffHeapStash#get(String, boolean)} when a null pointer exception
     * is
     * thrown. We should be mocking the behavior of HTreeMap.get(), but it cannot be
     * mocked so we throw the null pointer elsewhere instead.
     */
    @Test
    public void testGet_nullPointerException() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true);
        doThrow(NullPointerException.class).when(mockTTLTimeWheel).remove(anyString());

        /* Call method */
        String result = stash.get("key1", false);

        /* Test assertions */
        assertEquals(ProtocolUtil.buildErrorResponse(OffHeapStash.DB_CLOSED_ERROR), result);
    }

    /**
     * Test {@link OffHeapStash#get(String, boolean)} when an illegal access error
     * is
     * thrown. We should be mocking the behavior of HTreeMap.get(), but it cannot be
     * mocked so we throw the null pointer elsewhere instead.
     */
    @Test
    public void testGet_IllegalAccessError() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(true);
        doThrow(IllegalAccessError.class).when(mockTTLTimeWheel).remove(anyString());

        /* Call method */
        String result = stash.get("key1", false);

        /* Test assertions */
        assertEquals(ProtocolUtil.buildErrorResponse(OffHeapStash.DB_CLOSED_ERROR), result);
    }

    /**
     * Test {@link OffHeapStash#contains(String)}.
     */
    @Test
    public void testContains() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(false);

        /* Test assertions */
        assertEquals(true, stash.contains("key1", false));
        assertEquals(false, stash.contains("key2", false));
    }

    /**
     * Test {@link OffHeapStash#delete(String)}.
     */
    @Test
    public void testDelete() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(false);

        /* Call method */
        stash.delete("key1");

        /* Test assertions */
        assertEquals(false, stash.contains("key2", false));
        verify(mockTTLTimeWheel, times(1)).remove(anyString());
    }

    /**
     * Test {@link OffHeapStash#setWithTTL(String, String, long)}.
     */
    @Test
    public void testSetWithTTL() {
        /* Setup */
        when(mockTTLTimeWheel.isExpired(anyString()))
                .thenReturn(false);

        /* Call method */
        stash.setWithTTL("key1", "value1", 100L);

        /* Test assertions */
        assertEquals(true, stash.contains("key1", false));
        verify(mockTTLTimeWheel, times(1)).add(anyString(), anyLong());
    }

    /**
     * Test {@link OffHeapStash#updateTTL(String, long)}.
     */
    @Test
    public void testUpdateTTL() {
        /* Populate stash */
        stash.set("key1", "value1");

        /* Call method */
        boolean updatedTTL = stash.updateTTL("key1", 100L);

        /* Test assertions */
        assertEquals(true, updatedTTL);
        verify(mockTTLTimeWheel, times(1)).add(anyString(), anyLong());
    }

    /**
     * Test {@link OffHeapStash#updateTTL(String, long)} when the key doesn't exist.
     */
    @Test
    public void testUpdateTTL_keyDoesntExist() {
        /* Call method */
        boolean updatedTTL = stash.updateTTL("key1", 100L);

        /* Test assertions */
        assertEquals(false, updatedTTL);
        verify(mockTTLTimeWheel, never()).add(anyString(), anyLong());
    }

    /**
     * Test {@link OffHeapStash#drop()}.
     */
    @Test
    public void testDrop() throws IOException {
        doNothing().when(mockSnapshotWriter).close();
        /* Call method */
        stash.drop();

        /* Test assertions */
        assertEquals(ProtocolUtil.buildErrorResponse(OffHeapStash.DB_CLOSED_ERROR), stash.get("key1", false));
    }

    /**
     * Test {@link OffHeapStash#drop()} with an IO exception..
     */
    @Test
    public void testDrop_IOException() throws IOException {
        doThrow(IOException.class).when(mockSnapshotWriter).close();
        /* Call method */
        stash.drop();

        /* Test assertions */
        assertEquals(ProtocolUtil.buildErrorResponse(OffHeapStash.DB_CLOSED_ERROR), stash.get("key1", false));
    }

    /**
     * Test {@link OffHeapStash#updateTTL(String, long)}.
     */
    @Test
    public void testExpireTTLKeys() {
        /* Setup */
        when(mockTTLTimeWheel.expireKeys()).thenReturn(List.of("key1", "key2"));

        /* Call method */
        stash.expireTTLKeys();

        /* Test assertions */
        verify(mockTTLTimeWheel, times(1)).expireKeys();
        verify(mockLogger, times(1)).debug(anyString());
    }

    /**
     * Test {@link OffHeapStash#updateTTL(String, long)} when no keys were expired.
     */
    @Test
    public void testExpireTTLKeys_noneExpired() {
        /* Setup */
        when(mockTTLTimeWheel.expireKeys()).thenReturn(Collections.emptyList());

        /* Call method */
        stash.expireTTLKeys();

        /* Test assertions */
        verify(mockTTLTimeWheel, times(1)).expireKeys();
        verify(mockLogger, never()).debug(anyString());
    }

    /**
     * Test {@link OffHeapStash#getInfo()}.
     */
    @Test
    public void testGetInfo() {
        String result = stash.getInfo();
        assertEquals("- Number of keys: \t0\n" + //
                        "- Max keys allowed: \t1000000\n" + //
                        "- Off-heap: \t\ttrue\n" + //
                        "- Snapshots enabled: \ttrue\n", result);
    }

    /**
     * Test {@link OffHeapStash#evictKeys()}.
     * @throws IOException 
     */
    @Test
    public void testEvict() throws IOException {
        cache.put("key1", "val1");
        cache.put("key2", "val2");
        cache.put("key3", "val3");
        stash = new OffHeapStash(db, cache, mockTTLTimeWheel, mockLogger, mockEvictionTracker, "testStash",
                1, StashManager.DEFAULT_STASH_ENABLE_BACKUPS, mockSnapshotWriterFactory); /* Set max key count to 1 */

        when(mockEvictionTracker.evict())
                .thenReturn("key1")
                .thenReturn("key2");

        /* Evict keys */
        stash.evictKeys();

        /* Stash size should be 1 now */
        assertEquals(1, cache.size());
    }

    /**
     * Test {@link OffHeapStash#evictKeys()} when there's no more keys to evict.
     * @throws IOException 
     */
    @Test
    public void testEvict_noMoreToEvict() throws IOException {
        cache.put("key1", "val1");
        cache.put("key2", "val2");
        cache.put("key3", "val3");
        stash = new OffHeapStash(db, cache, mockTTLTimeWheel, mockLogger, mockEvictionTracker, "testStash",
                1, StashManager.DEFAULT_STASH_ENABLE_BACKUPS, mockSnapshotWriterFactory); /* Set max key count to 1 */

        when(mockEvictionTracker.evict())
                .thenReturn(null);

        /* Evict keys */
        stash.evictKeys();

        /* Stash size should be 3 since evictKeys returned null */
        assertEquals(3, cache.size());
    }

    /**
     * Test {@link OffHeapStash#clear()}.
     * @throws IOException 
     */
    @Test
    public void testClear() throws IOException {
        cache.put("key1", "val1");
        cache.put("key2", "val2");
        cache.put("key3", "val3");
        stash = new OffHeapStash(db, cache, mockTTLTimeWheel, mockLogger, mockEvictionTracker, "testStash",
                Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS, mockSnapshotWriterFactory);

        doNothing().when(mockEvictionTracker).clear();
        doNothing().when(mockTTLTimeWheel).clear();

        /* Evict keys */
        stash.clear();

        /* Stash size should be 3 since evictKeys returned null */
        assertEquals(0, cache.size());
        verify(mockEvictionTracker).clear();
        verify(mockTTLTimeWheel).clear();
    }
}
