package com.youngbryanyu.simplistash.stash.backups;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.stash.Stash;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BackupManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Backupable stash;
    private final SnapshotWriter snapshotWriter;
    private boolean backupNeeded = false;

    @Autowired
    public BackupManager(Backupable stash, SnapshotWriter snapshotWriter) {
        this.stash = stash;
        this.snapshotWriter = snapshotWriter;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::backup, 0, Stash.BACKUP_DELAY_S, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    // TODO: every new backup started should overwrite previous
    private void backup() {
        try {
            if (backupNeeded) {
                snapshotWriter.writeMetadata(stash.getName(), stash.getMaxKeyCount());
                for (Map.Entry<String, String> entry : stash.getAllEntries().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    long expirationTime = stash.getExpirationTime(key);
                    snapshotWriter.writeEntry(key, value, expirationTime);
                }
                snapshotWriter.close();
                backupNeeded = false;
            }

            // TODO: when the new backup is done, delete old backup and old WAL
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO:
     * - flush new backup file and overwrite previous after backup done
     * - new WAL to be started when starting backup
     */

    public void markBackupNeeded() {
        backupNeeded = true;
    }
}

// TODO: add javadoc and clean
