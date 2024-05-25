package com.youngbryanyu.simplistash.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the file util.
 */
public class FileUtilTest {
    /*
     * The test existing dir.
     */
    private static final String EXISTING_DIR = "testDirExists";
    /**
     * The test new dir.
     */
    private static final String NEW_DIR = "testDirNew";

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws IOException {
        /* Create a directory that already exists */
        File existingDir = new File(EXISTING_DIR);
        if (!existingDir.exists()) {
            if (!existingDir.mkdirs()) {
                throw new IOException("Failed to create test directory: " + EXISTING_DIR);
            }
        }
    }

    /**
     * Tear down the files created for the tests.
     */
    @AfterEach
    public void tearDown() {
        /* Clean up test directories */
        deleteDirectory(new File(EXISTING_DIR));
        deleteDirectory(new File(NEW_DIR));
    }

    /**
     * Helper method to delete a directory for cleanup.
     * 
     * @param directory The directory to delete.
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Test ensuring the directory exists with an existing directory.
     */
    @Test
    public void testEnsureDirectoryExists_ExistingDirectory() throws IOException {
        FileUtil.ensureDirectoryExists(EXISTING_DIR);
        File directory = new File(EXISTING_DIR);
        assertTrue(directory.exists() && directory.isDirectory());
    }

    /**
     * Test ensuring the directory exists with a new directory.
     */
    @Test
    public void testEnsureDirectoryExists_NewDirectory() throws IOException {
        FileUtil.ensureDirectoryExists(NEW_DIR);
        File directory = new File(NEW_DIR);
        assertTrue(directory.exists() && directory.isDirectory());
    }

    /**
     * Test ensuring the directory exists but a failure occurs.
     */
    @Test
    public void testEnsureDirectoryExists_Failure() throws IOException {
        // Simulate a scenario where directory creation fails
        FileUtil.ensureDirectoryExists(NEW_DIR + "/nestedDir");
        File directory = new File(NEW_DIR + "/nestedDir");
        assertTrue(directory.exists() && directory.isDirectory());
        directory.setReadOnly(); // Make the directory read-only to simulate failure

        IOException exception = assertThrows(IOException.class, () -> {
            FileUtil.ensureDirectoryExists(NEW_DIR + "/nestedDir/failureTest");
        });

        assertEquals("Failed to create directories: " + NEW_DIR + "/nestedDir/failureTest", exception.getMessage());
        directory.setWritable(true); // Clean up by making the directory writable again
    }
}
