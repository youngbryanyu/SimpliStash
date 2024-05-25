package com.youngbryanyu.simplistash.stash;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.youngbryanyu.simplistash.stash.replication.ReplicaHandler;
import com.youngbryanyu.simplistash.stash.replication.ReplicaHandlerFactory;
import com.youngbryanyu.simplistash.stash.snapshots.SnapshotWriter;
import com.youngbryanyu.simplistash.utils.FileUtil;
import com.youngbryanyu.simplistash.utils.SerializationUtil;

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
     * The mocked off heap stash.
     */
    @Mock
    private OffHeapStash mockOffHeapStash;
    /**
     * The mocked on heap stash.
     */
    @Mock
    private OnHeapStash mockOnHeapStash;
    /**
     * The mocked logger.
     */
    @Mock
    private Logger mockLogger;
    /**
     * The mock replica factory.
     */
    @Mock
    private ReplicaHandlerFactory mockReplicaFactory;
    /**
     * The mocked replica handler.
     */
    @Mock
    private ReplicaHandler mockReplicaHandler;
    /**
     * The mocked socket.
     */
    @Mock
    private Socket mockSocket;
    /**
     * The stash manager under test.
     */
    private StashManager stashManager;

   /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(mockStashFactory.createOffHeapStash(anyString(), anyLong(), anyBoolean())).thenReturn(mockOffHeapStash);
        when(mockStashFactory.createOnHeapStash(anyString(), anyLong(), anyBoolean())).thenReturn(mockOnHeapStash);
        when(mockReplicaFactory.createReplica(anyString(), anyInt())).thenReturn(mockReplicaHandler);
        when(mockReplicaHandler.getSocket()).thenReturn(mockSocket);
        when(mockSocket.getInetAddress()).thenReturn(InetAddress.getLocalHost()); /* use localhost */
        stashManager = new StashManager(mockStashFactory, mockReplicaFactory, mockLogger);
    }

    /**
     * Tear down after each test.
     */
    @AfterEach
    public void tearDown() throws IOException {
        // Clean up created files and directories
        File directory = new File(SnapshotWriter.DIR);
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
            // directory.delete();
        }
    }

    /**
     * Test {@link StashManager#createStash(String, boolean)} with an off heap stash.
     */
    @Test
    public void testCreateStash_offHeap() {
        assertTrue(stashManager.createStash("stash1", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS));
        assertTrue(stashManager.containsStash("stash1"));
    }

    /**
     * Test {@link StashManager#createStash(String, boolean)} with an on heap stash.
     */
    @Test
    public void testCreateStash_onHeap() {
        assertTrue(stashManager.createStash("stash1", false, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS));
        assertTrue(stashManager.containsStash("stash1"));
    }

    /**
     * Test {@link StashManager#createStash(String)} when the stash name is already
     * taken.
     */
    @Test
    public void testCreateStash_alreadyExists() {
        stashManager.createStash(StashManager.DEFAULT_STASH_NAME, true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        assertEquals(1, stashManager.getNumStashes());
        stashManager.createStash("stash2", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.createStash("stash2", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        assertEquals(2, stashManager.getNumStashes()); 
    }

    /**
     * Test {@link StashManager#createStash(String)} when the stash limit is
     * reached.
     */
    @Test
    public void testCreateStash_maxLimitReached() {
        for (int i = 0; i < StashManager.MAX_NUM_STASHES; i++) {
            stashManager.createStash("Stash" + i, true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        }
        assertFalse(stashManager.createStash("StashLimitExceeded", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS));
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
     * @throws IOException 
     */
    @Test
    public void testDropStash() throws IOException {
        stashManager.createStash("stash1", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.dropStash("stash1");
        assertFalse(stashManager.containsStash("stash1"));
        verify(mockOffHeapStash).drop();
    }

    /**
     * Test {@link StashManager#dropStash(String)} when the stash doesn't exist.
     * @throws IOException 
     */
    @Test
    public void testDropStash_doesntExist() throws IOException {
        stashManager.dropStash("stash1");
        verify(mockOffHeapStash, never()).drop();
    }

    /**
     * Test {@link StashManager#expireTTLKeys()}.
     */
    @Test
    public void testExpireTTLKeys() {
        stashManager.createStash("stash1", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.createStash("stash2", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.expireTTLKeys();
        verify(mockOffHeapStash, atLeast(2)).expireTTLKeys();
    }

    /**
     * Test {@link StashManager#getStats()} for a master node.
     */
    @Test
    public void testGetStats_master() {
        stashManager.createStash("stash1", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.registerReadReplica("localhost", 3000);
        when(mockOffHeapStash.getInfo()).thenReturn("info");
        String result = stashManager.getStats();
        assertNotNull(result);
        assertTrue(result.contains("Heap memory stats"));
        assertTrue(result.contains("Non-heap memory stats"));
        assertTrue(result.contains("Disk stats"));
        assertTrue(result.contains("General stash stats"));
        assertTrue(result.contains("Specific stash stats"));
        assertTrue(result.contains("Replication:"));
        assertTrue(result.contains("Replica locations:"));
    }

    /**
     * Test {@link StashManager#getStats()} for a replica node.
     */
    @Test
    public void testGetStats_replica() {
        System.setProperty("masterIp", "localhost");
        System.setProperty("masterPort", "3000");

        stashManager.createStash("stash1", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.registerReadReplica("localhost", 3000);
        when(mockOffHeapStash.getInfo()).thenReturn("info");
        String result = stashManager.getStats();
        assertNotNull(result);
        assertTrue(result.contains("Heap memory stats"));
        assertTrue(result.contains("Non-heap memory stats"));
        assertTrue(result.contains("Disk stats"));
        assertTrue(result.contains("General stash stats"));
        assertTrue(result.contains("Specific stash stats"));
        assertTrue(result.contains("Replication:"));
        assertTrue(!result.contains("Replica locations:"));

        System.clearProperty("masterIp");
        System.clearProperty("masterPort");
    }

     /**
     * Test {@link StashManager#getStats()} for a replica node.
     */
    @Test
    public void testGetStats_invalidMasterPort() {
        System.setProperty("masterIp", "localhost");
        System.setProperty("masterPort", "invalid");

        stashManager.createStash("stash1", true, Stash.DEFAULT_MAX_KEY_COUNT, StashManager.DEFAULT_STASH_ENABLE_BACKUPS);
        stashManager.registerReadReplica("localhost", 3000);
        when(mockOffHeapStash.getInfo()).thenReturn("info");
        String result = stashManager.getStats();
        assertNotNull(result);
        assertTrue(result.contains("Heap memory stats"));
        assertTrue(result.contains("Non-heap memory stats"));
        assertTrue(result.contains("Disk stats"));
        assertTrue(result.contains("General stash stats"));
        assertTrue(result.contains("Specific stash stats"));
        assertTrue(result.contains("Replication:"));
        assertTrue(result.contains("Replica locations:"));

        System.clearProperty("masterIp");
        System.clearProperty("masterPort");
    }


    /**
     * Test {@link StashManager#initializeFromSnapshots()} with an off heap serialized stash.
     */
    @Test
    public void testInitializeFromSnapshots_offHeap() throws IOException {
        // Create a temporary snapshot file
        File directory = new File(SnapshotWriter.DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File snapshotFile = new File(directory, "default.snapshot");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(snapshotFile))) {
            writer.write(SerializationUtil.encode("default"));
            writer.write(SerializationUtil.encode("1000"));
            writer.write(SerializationUtil.encode("true"));
            writer.write(SerializationUtil.encode("key1"));
            writer.write(SerializationUtil.encode("value1"));
            writer.write(SerializationUtil.encode("123456789"));
            writer.write(SerializationUtil.encode("key2"));
            writer.write(SerializationUtil.encode("value2"));
            writer.write(SerializationUtil.encode("-1"));
        }

        stashManager.initializeFromSnapshots();

        assertTrue(stashManager.containsStash("default"));
        verify(mockStashFactory, atLeast(1)).createOffHeapStash(anyString(), anyLong(), anyBoolean());
        verify(mockOffHeapStash, atLeast(1)).setWithTTL(anyString(), anyString(), anyLong());
        verify(mockOffHeapStash, atLeast(1)).set(anyString(), anyString());
    }

    /**
     * Test {@link StashManager#initializeFromSnapshots()} with an IO exception.
     */
    @Test
    public void testInitializeFromSnapshots_IOException() throws IOException {
        // Create a temporary snapshot file
        File directory = new File(SnapshotWriter.DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File snapshotFile = new File(directory, "default.snapshot");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(snapshotFile))) {
            writer.write(SerializationUtil.encode("default"));
            writer.write(SerializationUtil.encode("1000"));
            writer.write(SerializationUtil.encode("true"));
            writer.write(SerializationUtil.encode("key2"));
            writer.write(SerializationUtil.encode("value2"));
            String shortened = SerializationUtil.encode("-1");
            writer.write(shortened.substring(0, shortened.length() - 1));
        }

        stashManager.initializeFromSnapshots();

        assertTrue(stashManager.containsStash("default"));
        verify(mockStashFactory, atLeast(1)).createOffHeapStash(anyString(), anyLong(), anyBoolean());
        verify(mockOffHeapStash, never()).setWithTTL(anyString(), anyString(), anyLong());
        verify(mockOffHeapStash, never()).set(anyString(), anyString());
    }

     /**
     * Test {@link StashManager#initializeFromSnapshots()} with an on heap serialized stash.
     */
    @Test
    public void testInitializeFromSnapshots_onHeap() throws IOException {
        // Create a temporary snapshot file
        File directory = new File(SnapshotWriter.DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File snapshotFile = new File(directory, "default.snapshot");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(snapshotFile))) {
            writer.write(SerializationUtil.encode("default"));
            writer.write(SerializationUtil.encode("1000"));
            writer.write(SerializationUtil.encode("false"));
            writer.write(SerializationUtil.encode("key1"));
            writer.write(SerializationUtil.encode("value1"));
            writer.write(SerializationUtil.encode("123456789"));
            writer.write(SerializationUtil.encode("key2"));
            writer.write(SerializationUtil.encode("value2"));
            writer.write(SerializationUtil.encode("-1"));
        }

        stashManager.initializeFromSnapshots();

        assertTrue(stashManager.containsStash("default"));
        verify(mockStashFactory, atLeast(1)).createOnHeapStash(anyString(), anyLong(), anyBoolean());
        // verify(mockOffHeapStash, atLeast(1)).setWithTTL(anyString(), anyString(), anyLong());
        // verify(mockOffHeapStash, atLeast(1)).set(anyString(), anyString());
    }

    /**
     * Test {@link StashManager#registerReadReplica(String, int)}.
     */
    @Test
    public void testRegisterReadReplica() {
        stashManager.registerReadReplica("127.0.0.1", 8080);
        verify(mockReplicaHandler, times(1)).connect();
        assertEquals(1, stashManager.getReplicaHandlers().size());
    }

    /**
     * Test {@link StashManager#forwardCommandToReadReplicas(String)}.
     */
    @Test
    public void testForwardCommandToReadReplicas() {
        stashManager.registerReadReplica("127.0.0.1", 8080);
        stashManager.forwardCommandToReadReplicas("SET key value");
        verify(mockReplicaHandler, times(1)).forwardCommand("SET key value");
    }
}
