package com.youngbryanyu.simplistash.utils;

import java.io.File;
import java.io.IOException;

/**
 * File utils.
 */
public class FileUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private FileUtil() {
    }

    /**
     * Ensures that a directory exists and creates if it it doesn't.
     * 
     * @param directoryPath The directory path.
     * @throws IOException If an IOException occurs.
     */
    public static void ensureDirectoryExists(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directories: " + directoryPath);
            }
        }
    }
}
