package com.youngbryanyu.simplistash.stash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

/**
 * Unit tests for the stash manager.
 */
public class StashManagerTest {
    /**
     * The mocked stash factory.
     */
    @Mock
    private StashFactory mockStashFactory;
    /**
     * The mocked stash.
     */
    @Mock
    private OffHeapStash mockStash;
    /**
     * The mocked logger.
     */
    @Mock
    private Logger mockLogger;
    /**
     * The stash manager under test.
     */
    private StashManager stashManager;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockStashFactory.createOffHeapStash(anyString())).thenReturn(mockStash);
        stashManager = new StashManager(mockStashFactory, mockLogger);
    }

    /**
     * Test {@link StashManager#createStash(String)}.
     */
    @Test
    public void testCreateStash() {
        assertTrue(stashManager.createStash("stash1", true));
        assertTrue(stashManager.containsStash("stash1"));
    }

    /**
     * Test {@link StashManager#createStash(String)} when the stash name is already
     * taken.
     */
    @Test
    public void testCreateStashAlreadyExists() {
        stashManager.createStash(StashManager.DEFAULT_STASH_NAME, true);
        assertEquals(1, stashManager.getNumStashes());
        stashManager.createStash("stash2", true);
        stashManager.createStash("stash2", true);
        assertEquals(2, stashManager.getNumStashes());
    }

    /**
     * Test {@link StashManager#createStash(String)} when the stash limit is
     * reached.
     */
    @Test
    public void testCreateStashMaxLimitReached() {
        for (int i = 0; i < StashManager.MAX_NUM_STASHES; i++) {
            stashManager.createStash("Stash" + i, true);
        }
        assertFalse(stashManager.createStash("StashLimitExceeded", true));
    }

    /**
     * Test {@link StashManager#getStash(String)}.
     */
    @Test
    public void testGetStash() {
        assertNotNull(stashManager.getStash(StashManager.DEFAULT_STASH_NAME));
        assertNull(stashManager.getStash("nonExistent"));
    }

    /**
     * Test {@link StashManager#containsStash(String)}.
     */
    @Test
    public void testContainsStash() {
        assertTrue(stashManager.containsStash(StashManager.DEFAULT_STASH_NAME));
        assertFalse(stashManager.containsStash("nonExistent"));
    }

    /**
     * Test {@link StashManager#dropStash(String)}.
     */
    @Test
    public void testDropStash() {
        stashManager.createStash("stash1", true);
        stashManager.dropStash("stash1");
        assertFalse(stashManager.containsStash("stash1"));
        verify(mockStash).drop();
    }

    /**
     * Test {@link StashManager#dropStash(String)} when the stash doesn't exist.
     */
    @Test
    public void testDropStash_doesntExist() {
        stashManager.dropStash("stash1");
        verify(mockStash, never()).drop();
    }

    /**
     * Test {@link StashManager#expireTTLKeys()}.
     */
    @Test
    public void testExpireTTLKeys() {
        stashManager.createStash("stash1", true);
        stashManager.createStash("stash2", true);
        stashManager.expireTTLKeys();
        verify(mockStash, atLeast(2)).expireTTLKeys();
    }
}
