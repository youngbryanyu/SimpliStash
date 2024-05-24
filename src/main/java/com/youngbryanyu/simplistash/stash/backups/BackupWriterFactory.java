package com.youngbryanyu.simplistash.stash.backups;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The factory class to create snapshot and WAL writers.
 */
@Component
public class BackupWriterFactory {
    /**
     * The spring application context
     */
    private ApplicationContext context;

    @Autowired
    public BackupWriterFactory(ApplicationContext context) {
    }

    public SnapshotWriter getSnapshotWriter(String stashName) throws IOException {
        return context.getBean(SnapshotWriter.class, stashName);
    }
}


// TODO: add javadoc