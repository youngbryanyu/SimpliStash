package com.youngbryanyu.simplistash.stash.backups;

import java.util.Map;

public interface Backupable {
    public String getName();

    public boolean isBackupEnabled();

    public long getMaxKeyCount();

    public long getExpirationTime(String key);

    public Map<String, String> getAllEntries();
}

// TODO: add javadoc ON HEAP
