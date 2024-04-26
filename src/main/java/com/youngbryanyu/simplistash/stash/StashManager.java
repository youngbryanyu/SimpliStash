package com.youngbryanyu.simplistash.stash;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

/**
 * A class to manage all the stashes.
 */
public class StashManager {
    /**
     * The name of the default stash used if none is specified by the client.
     */
    private static final String DEFAULT_STASH_NAME = "default";
    /**
     * The singleton instance of the stash manager
     */
    private static StashManager instance;
    /**
     * Returns the the DB instance. Each DB has its own transaction session.
     */
    private DB dbInstance;
    /**
     * Mapping of each stash's name to its cache.
     */
    private Map<String, HTreeMap<String, String>> stashes;

    /* Private constructor for singleton access */
    private StashManager() {
        dbInstance = DBMaker.memoryDirectDB().make();
        stashes = new HashMap<>();
    }

    public static StashManager getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    public static void initialize() {
        if (instance == null) {
            instance = new StashManager();
        }
    }

    public static String getDefaultStashName() {
        return DEFAULT_STASH_NAME;
    }
}
