package com.youngbryanyu.simplistash.stash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final String DEFAULT_STASH_NAME = "default";
    /**
     * The factory used to create new stash instances.
     */
    private final StashFactory stashFactory;
    /**
     * Mapping of each stash's name to its instance. Needs to be thread-safe since
     * we can have multiple NIO worker threads on the server.
     */
    private final Map<String, Stash> stashes;

    /**
     * Constructor for a stash manager.
     * 
     * @param stashFactory The factory used to create the stashes.
     */
    @Autowired
    public StashManager(StashFactory stashFactory) {
        this.stashFactory = stashFactory;
        stashes = new ConcurrentHashMap<>();

        /* Create the default stash */
        createDefaultStash();
    }

    /**
     * Creates the default stash to be used.
     */
    private void createDefaultStash() {
        createStash(DEFAULT_STASH_NAME);
    }

    /**
     * Creates a new stash with the given name and stores it in the stashes map.
     * 
     * @param name The name of the stash.
     */
    public void createStash(String name) {
        stashes.put(name, stashFactory.createStash());
    }

    /**
     * Gets a stash that matches the input name.
     * 
     * @param name The name of the stash.
     * @return Returns the stash corresponding to the name.
     */
    public Stash getStash(String name) {
        return stashes.get(name);
    }
}
