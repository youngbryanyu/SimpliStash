package com.youngbryanyu.simplistash.stash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DB.HashMapMaker;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

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
     * The mock stash.
     */
    @Mock
    private OffHeapStash mockOffHeapStash;
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
        when(mockDB.hashMap(anyString(), any(), any())).thenReturn(mockHashmapMaker);
        when(mockHashmapMaker.create()).thenReturn(null);

        stashFactory = new StashFactory(mockContext);
    }

    /**
     * Test {@link StashFactory#createStash(String)}.
     */
    @Test
    void testCreateOffHeapStash() {
        /* Setup */
        String stashName = "testStash";
        when(mockContext.getBean(eq(OffHeapStash.class), any(), any(), any(), any(), anyString()))
                .thenReturn(mockOffHeapStash);

        /* Call method */
        Stash stash = stashFactory.createStash(stashName);

        /* Test assertions */
        verify(mockContext).getBean(DB.class);
        verify(mockContext).getBean(TTLTimeWheel.class);
        verify(mockContext).getBean(Logger.class);
        verify(mockContext).getBean(OffHeapStash.class, mockDB, null, mockTTLTimeWheel, mockLogger, stashName);
        assertNotNull(stash);
        assertEquals(mockOffHeapStash, stash);
    }
}
