package com.youngbryanyu.simplistash.stash.snapshots;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.utils.FileUtil;
import com.youngbryanyu.simplistash.utils.SerializationUtil;

/**
 * The snapshot writer.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SnapshotWriter {
    /**
     * The snapshot file extension.
     */
    public static final String EXTENSION = "snapshot";
    /**
     * The path to the directory holding the WAL files.
     */
    public static final String DIR = "./snapshot_files/";
    /**
     * The temp file path before committing.
     */
    private Path tempFilePath;
    /**
     * The final file path after committing.
     */
    private Path finalFilePath;
    /**
     * The buffered writer to write to disk.
     */
    private BufferedWriter writer;
    /**
     * Stash name.
     */
    private final String name;
    /**
     * Whether snapshots are enabled.
     */
    private final boolean enableSnapshots;

    /**
     * The constructor.
     * 
     * @param name The stash name.
     * @throws IOException If an IO Exception occurs during initialization.
     */
    @Autowired
    public SnapshotWriter(String name, boolean enableSnapshots) throws IOException {
        this.name = name;
        this.enableSnapshots = enableSnapshots;
    }

    /**
     * Opens the snap shot writer.
     * 
     * @throws IOException If an IO exception occurs.
     */
    public void open() throws IOException {
        if (enableSnapshots) {
            /* Ensure base DIR exists */
            if (enableSnapshots) {
                FileUtil.ensureDirectoryExists(DIR);
            }

            /* Create temp and final file */
            tempFilePath = Path.of(DIR, name + "_new." + EXTENSION);
            finalFilePath = Path.of(DIR, name + "." + EXTENSION);

            /* Initialize writer with temp file in truncate mode */
            writer = new BufferedWriter(new FileWriter(tempFilePath.toFile(), false));
        }
    }

    /**
     * Writes metadata to the snapshot about a stash. Backups are enabled is
     * implied. Uses prefixed strings.
     * 
     * Metadata is serialized in the order:
     * - Name
     * - Max key count
     * - Off heap flag
     * 
     * @param stashName   The stash's name.
     * @param maxKeyCount The max key count.
     * @throws IOException
     */
    public void writeMetadata(String stashName, long maxKeyCount, boolean offHeap) throws IOException {
        if (enableSnapshots) {
            writer.write(SerializationUtil.encode(stashName));
            writer.write(SerializationUtil.encode(Long.toString(maxKeyCount)));
            writer.write(SerializationUtil.encode(Boolean.toString(offHeap)));
        }
    }

    /**
     * Writes an entry to the snapshot. Uses prefixed strings.
     * 
     * @param key            The key.
     * @param value          The value.
     * @param expirationTime The expiration time.
     * @throws IOException If an IOException occurs.
     */
    public void writeEntry(String key, String value, long expirationTime) throws IOException {
        if (enableSnapshots) {
            writer.write(SerializationUtil.encode(key));
            writer.write(SerializationUtil.encode(value));
            writer.write(SerializationUtil.encode(Long.toString(expirationTime)));
        }
    }

    /**
     * Closes the writer. Should flush to disk first before closing.
     * 
     * @throws IOException If an IOException occurs.
     */
    public void close() throws IOException {
        if (enableSnapshots) {
            writer.close();
        }
    }

    /**
     * Commits a snapshot. By renaming the temp file to the final file.
     * 
     * @throws IOException
     */
    public void commit() throws IOException {
        writer.flush();
        Files.move(tempFilePath, finalFilePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
