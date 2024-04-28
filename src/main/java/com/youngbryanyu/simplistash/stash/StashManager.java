package com.youngbryanyu.simplistash.stash;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A class to manage all the stashes.
 */
@Component
public class StashManager {
    /**
     * The name of the default stash used if none is specified by the client.
     */
    private static final String DEFAULT_STASH_NAME = "default";
    /**
     * Returns the the DB instance. Each DB has its own transaction session.
     */
    private DB db;
    /**
     * Mapping of each stash's name to its cache.
     */
    private Map<String, HTreeMap<String, String>> stashes;

    /* Private constructor for singleton access */
    @Autowired
    private StashManager(DB db) {
        stashes = new HashMap<>();
    }

    public static String getDefaultStashName() {
        return DEFAULT_STASH_NAME;
    }
}
