package com.youngbryanyu.simplistash.stash.snapshots;

import com.youngbryanyu.simplistash.utils.FileUtil;
import com.youngbryanyu.simplistash.utils.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the snapshot writer.
 */
class SnapshotWriterTest {
    /**
     * The stash name.
     */
    private static final String STASH_NAME = "testStash6921";
    /**
     * The dir for testing files.
     */
    private static final String DIR = "./snapshot_files/";
    /**
     * The temp file name.
     */
    private static final String TEMP_FILE_NAME = DIR + STASH_NAME + "_temp.snapshot";
    /**
     * The final file name.
     */
    private static final String FINAL_FILE_NAME = DIR + STASH_NAME + ".snapshot";
    /**
     * The key.
     */
    private static final String KEY = "key";
    /**
     * The value/
     */
    private static final String VALUE = "value";
    /**
     * The expiration time.
     */
    private static final long EXPIRATION_TIME = 123456789L;
    /**
     * The snapshot writer.
     */
    private SnapshotWriter snapshotWriter;
    /**
     * The buffered writer.
     */
    private BufferedWriter mockWriter;
    /**
     * THe mocked file writer.
     */
    private MockedConstruction<FileWriter> mockedFileWriter;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws IOException {
        mockWriter = mock(BufferedWriter.class);
        mockedFileWriter = mockConstruction(FileWriter.class, (mock, context) -> {
            // Do nothing, since we'll mock the BufferedWriter directly
        });

        snapshotWriter = spy(new SnapshotWriter(STASH_NAME, true));

        // Mock the BufferedWriter used by FileWriter
        doReturn(mockWriter).when(snapshotWriter).createBufferedWriter(any());
    }

    /**
     * Cleanup after each test.
     */
    @AfterEach
    public void tearDown() throws IOException {
        mockedFileWriter.close();
        Files.deleteIfExists(Path.of(TEMP_FILE_NAME));
        Files.deleteIfExists(Path.of(FINAL_FILE_NAME));
    }

    /**
     * Test opening a file
     */
    @Test
    public void testOpen() throws IOException {
        try (MockedStatic<FileUtil> fileUtilMockedStatic = mockStatic(FileUtil.class)) {
            fileUtilMockedStatic.when(() -> FileUtil.ensureDirectoryExists(DIR)).thenAnswer(invocation -> {
                File dir = new File(DIR);
                if (!dir.exists()) {
                    return dir.mkdirs();
                }
                return true;
            });

            snapshotWriter.open();

            fileUtilMockedStatic.verify(() -> FileUtil.ensureDirectoryExists(DIR), times(1));
            verify(mockWriter, never()).write(anyString());
        }
    }

    /**
     * Test writing metadata.
     */
    @Test
    public void testWriteMetadata() throws IOException {
        snapshotWriter.open();
        snapshotWriter.writeMetadata(STASH_NAME, 1000L, true);
        verify(mockWriter, times(1)).write(SerializationUtil.encode(STASH_NAME));
        verify(mockWriter, times(1)).write(SerializationUtil.encode(Long.toString(1000L)));
        verify(mockWriter, times(1)).write(SerializationUtil.encode(Boolean.toString(true)));
    }

    /**
     * Test writing an entry.
     */
    @Test
    public void testWriteEntry() throws IOException {
        snapshotWriter.open();
        snapshotWriter.writeEntry(KEY, VALUE, EXPIRATION_TIME);
        verify(mockWriter, times(1)).write(SerializationUtil.encode(KEY));
        verify(mockWriter, times(1)).write(SerializationUtil.encode(VALUE));
        verify(mockWriter, times(1)).write(SerializationUtil.encode(Long.toString(EXPIRATION_TIME)));
    }

    /**
     * Test committing a snapshot.
     */
    @Test
    public void testCommit() throws IOException {
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            snapshotWriter.open();
            snapshotWriter.commit();
            verify(mockWriter, times(1)).flush();
            filesMockedStatic.verify(() -> Files.move(Path.of(TEMP_FILE_NAME), Path.of(FINAL_FILE_NAME),
                    StandardCopyOption.REPLACE_EXISTING), times(1));
        }
    }

    /**
     * Test deleting a snapshot.
     */
    @Test
    public void testDelete() throws IOException {
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            snapshotWriter.open();
            snapshotWriter.delete();
            filesMockedStatic.verify(() -> Files.deleteIfExists(Path.of(TEMP_FILE_NAME)), times(1));
            filesMockedStatic.verify(() -> Files.deleteIfExists(Path.of(FINAL_FILE_NAME)), times(1));
        }
    }

    /**
     * Test closing the snapshot writer.
     */
    @Test
    public void testClose() throws IOException {
        snapshotWriter.open();
        snapshotWriter.close();
        verify(mockWriter, times(1)).close();
    }

    /**
     * Test when snapshots are disabled.
     */
    @Test
    public void testEnableSnapshotsFalse() throws IOException {
        SnapshotWriter snapshotWriterDisabled = new SnapshotWriter(STASH_NAME, false);
        snapshotWriterDisabled.open();
        snapshotWriterDisabled.writeMetadata(STASH_NAME, 1000L, true);
        snapshotWriterDisabled.writeEntry(KEY, VALUE, EXPIRATION_TIME);
        snapshotWriterDisabled.commit();
        snapshotWriterDisabled.close();
        assertFalse(Files.exists(Path.of(TEMP_FILE_NAME)));
        assertFalse(Files.exists(Path.of(FINAL_FILE_NAME)));
    }

    /**
     * Test create buffered writer.
     */
    @Test
    public void testCreateBufferedWriter() throws IOException {
        SnapshotWriter snapshotWriterDisabled = new SnapshotWriter(STASH_NAME, true);
        assertNotNull(snapshotWriterDisabled.createBufferedWriter(Path.of("/")));
    }
}
