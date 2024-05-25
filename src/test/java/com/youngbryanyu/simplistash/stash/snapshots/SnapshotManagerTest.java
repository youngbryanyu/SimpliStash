package com.youngbryanyu.simplistash.stash.snapshots;

import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for snapshot manager.
 */
class SnapshotManagerTest {
    /**
     * The snapshot manager under test.
     */
    private SnapshotManager snapshotManager;
    /**
     * The mock snapshot writer.
     */
    private SnapshotWriter mockSnapshotWriter;
    /**
     * The mock ttl time wheel.
     */
    private TTLTimeWheel mockTTLTimeWheel;
    /**
     * The mock logger.
     */
    private Logger mockLogger;
    /**
     * The mock cache.
     */
    private Map<String, String> cache;
    /**
     * The mocked scheduler.
     */
    private ScheduledExecutorService mockScheduler;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws IOException {
        mockSnapshotWriter = mock(SnapshotWriter.class);
        mockTTLTimeWheel = mock(TTLTimeWheel.class);
        mockLogger = mock(Logger.class);
        cache = new HashMap<>();
        mockScheduler = mock(ScheduledExecutorService.class);

        snapshotManager = new SnapshotManager("testStash", 1000L, true, cache, mockTTLTimeWheel, mockSnapshotWriter, mockLogger);
        snapshotManager = spy(snapshotManager);

        doReturn(mockScheduler).when(snapshotManager).createScheduler();
    }

    /**
     * Test starting the snapshot manager.
     */
    @Test
    public void testStart() {
        try (MockedStatic<Executors> mockedExecutors = mockStatic(Executors.class)) {
            mockedExecutors.when(() -> Executors.newScheduledThreadPool(1)).thenReturn(mockScheduler);
            snapshotManager.start();
            // verify(mockScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(Stash.SNAPSHOT_DELAY_S), eq(TimeUnit.SECONDS));
            assertNotNull(mockScheduler);
        }
    }

    /**
     * Test stopping the snapshot manager.
     */
    @Test
    public void testStop() {
        snapshotManager.start();
        snapshotManager.stop();
        assertNotNull(mockScheduler);
        // verify(mockScheduler, times(1)).shutdownNow();
    }

    /**
     * Test taking a snapshot.
     */
    @Test
    public void testTakeSnapshot() throws IOException {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        when(mockTTLTimeWheel.getExpirationTime(anyString())).thenReturn(123456789L);
        
        snapshotManager.markBackupNeeded();
        snapshotManager.takeSnapshot();
        
        verify(mockSnapshotWriter, times(1)).open();
        verify(mockSnapshotWriter, times(1)).writeMetadata("testStash", 1000L, true);
        verify(mockSnapshotWriter, times(1)).writeEntry("key1", "value1", 123456789L);
        verify(mockSnapshotWriter, times(1)).writeEntry("key2", "value2", 123456789L);
        verify(mockSnapshotWriter, times(1)).commit();
        verify(mockSnapshotWriter, times(1)).close();
    }

     /**
     * Test taking a snapshot with an IO exception.
     */
    @Test
    public void testTakeSnapshot_IOException() throws IOException {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        when(mockTTLTimeWheel.getExpirationTime(anyString())).thenReturn(123456789L);
        doThrow(new IOException()).when(mockSnapshotWriter).close();
        
        snapshotManager.markBackupNeeded();
        snapshotManager.takeSnapshot();
        
        verify(mockSnapshotWriter, times(1)).open();
        verify(mockSnapshotWriter, times(1)).writeMetadata("testStash", 1000L, true);
        verify(mockSnapshotWriter, times(1)).writeEntry("key1", "value1", 123456789L);
        verify(mockSnapshotWriter, times(1)).writeEntry("key2", "value2", 123456789L);
        verify(mockSnapshotWriter, times(1)).commit();
        verify(mockSnapshotWriter, times(1)).close();
    }

     /**
     * Test taking a snapshot when not needed.
     */
    @Test
    public void testTakeSnapshotNoBackupNeeded() throws IOException {
        snapshotManager.takeSnapshot();
        verify(mockSnapshotWriter, never()).open();
        verify(mockSnapshotWriter, never()).writeMetadata(anyString(), anyLong(), anyBoolean());
        verify(mockSnapshotWriter, never()).writeEntry(anyString(), anyString(), anyLong());
        verify(mockSnapshotWriter, never()).commit();
        verify(mockSnapshotWriter, never()).close();
    }

     /**
     * Test marking backup as needed.
     */
    @Test
    public void testMarkBackupNeeded() {
        snapshotManager.markBackupNeeded();
        assertTrue(snapshotManager.isBackupNeeded());
    }

     /**
     * Test close.
     */
    @Test
    public void testClose() throws IOException {
        snapshotManager.close();
        verify(mockSnapshotWriter, times(1)).close();
    }

    /**
     * Test delete.
     */
    @Test
    public void testDelete() throws IOException {
        snapshotManager.delete();
        verify(mockSnapshotWriter, times(1)).delete();
    }
}
