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

/**
 * Unit tests for the stash factory.
 */
public class StashFactoryTest {
    @Mock
    private ApplicationContext mockContext;
    @Mock
    private DB mockDB;
    @Mock
    private HashMapMaker<Object, Object> mockHashmapMaker;
    @Mock
    private TTLTimeWheel mockTTLTimeWheel;
    @Mock
    private Logger mockLogger;
    @Mock
    private Stash mockStash;

    private StashFactory stashFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getBean(DB.class)).thenReturn(mockDB);
        when(mockContext.getBean(TTLTimeWheel.class)).thenReturn(mockTTLTimeWheel);
        when(mockContext.getBean(Logger.class)).thenReturn(mockLogger);
        when(mockDB.hashMap(anyString(), any(), any())).thenReturn(mockHashmapMaker);
        when(mockHashmapMaker.create()).thenReturn(null);

        stashFactory = new StashFactory(mockContext);
    }

    @Test
    void testCreateStash() {
        /* Setup */
        String stashName = "testStash";
        when(mockContext.getBean(eq(Stash.class), any(), any(), any(), any(), anyString()))
                .thenReturn(mockStash);

        /* Call method */
        Stash stash = stashFactory.createStash(stashName);

        /* Test assertions */
        verify(mockContext).getBean(DB.class);
        verify(mockContext).getBean(TTLTimeWheel.class);
        verify(mockContext).getBean(Logger.class);
        verify(mockContext).getBean(Stash.class, mockDB, null, mockTTLTimeWheel, mockLogger, stashName);
        assertNotNull(stash);
        assertEquals(mockStash, stash);
    }
}
