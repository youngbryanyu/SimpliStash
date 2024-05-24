package com.youngbryanyu.simplistash.stash.backups;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.utils.SerializationUtil;

/**
 * The snapshot writer.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SnapshotWriter {
    /**
     * The WAL file extension.
     */
    private static final String EXTENSION = "snapshot";
    /**
     * The path to the directory holding the WAL files.
     */
    private static final String DIR = "./snapshots/";
    /**
     * The buffered writer to write to disk.
     */
    private final BufferedWriter writer;

    /**
     * The constructor.
     * 
     * @param stashName The stash name.
     * @throws IOException If an IO Exception occurs during initialization.
     */
    @Autowired
    public SnapshotWriter(String stashName) throws IOException {
        String snapshotFilePath = String.format("%s%s.%s", DIR, stashName, EXTENSION);
        writer = new BufferedWriter(new FileWriter(snapshotFilePath));
    }

    /**
     * Writes metadata to the snapshot about a stash. Backups are enabled is
     * implied. Uses prefixed strings.
     * 
     * @param stashName   The stash's name.
     * @param maxKeyCount The max key count.
     * @throws IOException
     */
    public void writeMetadata(String stashName, long maxKeyCount) throws IOException {
        writer.write(SerializationUtil.encode(stashName));
        writer.write(SerializationUtil.encode(Long.toString(maxKeyCount)));
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
        writer.write(SerializationUtil.encode(key));
        writer.write(SerializationUtil.encode(value));
        writer.write(SerializationUtil.encode(Long.toString(expirationTime)));
    }

    public void close() throws IOException {
        writer.close(); /* flushes the stream first */
    }
}

// TODO: flush

