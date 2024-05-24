package com.youngbryanyu.simplistash.stash.snapshots;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The factory class to create snapshot and WAL writers.
 */
@Component
public class SnapshotWriterFactory {
    /**
     * The spring application context
     */
    private ApplicationContext context;

    /**
     * The constructor.
     * 
     * @param context The spring application context.
     */
    @Autowired
    public SnapshotWriterFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Returns a new instance of a snapshot writer.
     * 
     * @param stashName       The stash name.
     * @param enableSnapshots Whether snapshots are enabled.
     * @return A new instance of a snapshot writer.
     * @throws IOException If an IO exception occurred.
     */
    public SnapshotWriter createSnapshotWriter(String stashName, boolean enableSnapshots) throws IOException {
        return context.getBean(SnapshotWriter.class, stashName, enableSnapshots);
    }
}
