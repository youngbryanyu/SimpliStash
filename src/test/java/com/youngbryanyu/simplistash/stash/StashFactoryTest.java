package com.youngbryanyu.simplistash.stash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DB.HashMapMaker;
import org.mapdb.HTreeMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import com.youngbryanyu.simplistash.eviction.EvictionTracker;
import com.youngbryanyu.simplistash.eviction.lru.LRUTracker;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotWriterFactory;
import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

/**
 * Unit tests for the stash factory.
 */
public class StashFactoryTest {
    /**
     * The mock application context for spring
     */
    @Mock
    private ApplicationContext mockContext;
    /**
     * The mock db.
     */
    @Mock
    private DB mockDB;
    /**
     * The mock hash map maker.
     */
    @Mock
    private HashMapMaker<Object, Object> mockHashmapMaker;
    /**
     * The mock ttl time wheel.
     */
    @Mock
    private TTLTimeWheel mockTTLTimeWheel;
    /**
     * The mock logger.
     */
    @Mock
    private Logger mockLogger;
    /**
     * The mock off heap stash.
     */
    @Mock
    private OffHeapStash mockOffHeapStash;
    /**
     * The mock on heap stash.
     */
    @Mock
    private OnHeapStash mockOnHeapStash;
    /**
     * The mock eviction tracker.
     */
    @Mock
    private LRUTracker mockEvictionTracker;
    /**
     * The mocked snapshot writer factory.
     */
    @Mock
    private SnapshotWriterFactory mockSnapshotWriterFactory;
    /**
     * The stash factory under test.
     */
    private StashFactory stashFactory;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getBean(DB.class)).thenReturn(mockDB);
        when(mockContext.getBean(TTLTimeWheel.class)).thenReturn(mockTTLTimeWheel);
        when(mockContext.getBean(Logger.class)).thenReturn(mockLogger);
        when(mockContext.getBean(LRUTracker.class)).thenReturn(mockEvictionTracker);
        when(mockContext.getBean(SnapshotWriterFactory.class)).thenReturn(mockSnapshotWriterFactory);
        when(mockDB.hashMap(anyString(), any(), any())).thenReturn(mockHashmapMaker);
        when(mockHashmapMaker.counterEnable()).thenReturn(mockHashmapMaker);
        when(mockHashmapMaker.create()).thenReturn(null); /* HTreeMap cannot be mocked */

        stashFactory = new StashFactory(mockContext);
    }

    /**
     * Test {@link StashFactory#createOffHeapStash(String)}.
     */
    @Test
    void testCreateOffHeapStash() {
        /* Setup */
        String stashName = "testStash";
        when(mockContext.getBean(eq(OffHeapStash.class), any(), any(), any(), any(), any(), anyString(), anyLong(), anyBoolean(), any()))
                .thenReturn(mockOffHeapStash);

        /* Call method */
        Stash stash = stashFactory.createOffHeapStash(stashName, Stash.DEFAULT_MAX_KEY_COUNT,
                StashManager.DEFAULT_ENABLE_BACKUPS);

        /* Test assertions */
        verify(mockContext).getBean(DB.class);
        verify(mockContext).getBean(TTLTimeWheel.class);
        verify(mockContext).getBean(Logger.class);
        verify(mockContext).getBean(LRUTracker.class);
        verify(mockContext).getBean(OffHeapStash.class,
                mockDB,
                null,
                mockTTLTimeWheel,
                mockLogger,
                mockEvictionTracker,
                stashName,
                Stash.DEFAULT_MAX_KEY_COUNT,
                StashManager.DEFAULT_ENABLE_BACKUPS,
                mockSnapshotWriterFactory);
        assertNotNull(stash);
        assertEquals(mockOffHeapStash, stash);
    }

    /**
     * Test {@link StashFactory#createOnHeapStash(String)}.
     */
    @Test
    void testCreateOnHeapStash() {
        /* Setup */
        String stashName = "testStash";
        when(mockContext.getBean(eq(OnHeapStash.class), any(), any(), any(), any(), anyString(), anyLong(),
                anyBoolean(), any()))
                .thenReturn(mockOnHeapStash);

        /* Call method */
        Stash stash = stashFactory.createOnHeapStash(stashName, Stash.DEFAULT_MAX_KEY_COUNT,
                StashManager.DEFAULT_ENABLE_BACKUPS);

        /* Test assertions */
        verify(mockContext).getBean(TTLTimeWheel.class);
        verify(mockContext).getBean(Logger.class);
        verify(mockContext).getBean(
                eq(OnHeapStash.class),
                any(Map.class),
                any(TTLTimeWheel.class),
                any(Logger.class),
                any(LRUTracker.class),
                anyString(),
                anyLong(),
                anyBoolean(),
                any(SnapshotWriterFactory.class));
        assertNotNull(stash);
        assertEquals(mockOnHeapStash, stash);
    }
}
