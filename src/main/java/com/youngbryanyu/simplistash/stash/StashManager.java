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
     * The name of the default stash used when none is specified by the client.
     */
    public static final String DEFAULT_STASH_NAME = "default";
    /**
     * The maximum number of stashes that can be created.
     */
    public static final int MAX_NUM_STASHES = 100;
    /**
     * The factory used to create new stash instances.
     */
    private final StashFactory stashFactory;
    /**
     * Mapping of each stash's name to its instance.
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

        /* Create default stash */
        createStash(DEFAULT_STASH_NAME);
    }

    /**
     * Creates a new stash with the given name and stores it in the stashes map.
     * Does nothing if the stash name is already taken. Fails if there are already
     * the max number of stashes supported.
     * 
     * @param name The name of the stash.
     * @return True if the stash was created successfully or already exists, false
     *         otherwise.
     */
    public boolean createStash(String name) {
        if (stashes.size() >= MAX_NUM_STASHES) {
            return false;
        }

        stashes.putIfAbsent(name, stashFactory.createStash(name));
        return true;
    }

    /**
     * Gets a stash that matches the given name.
     * 
     * @param name The name of the stash.
     * @return Returns the stash corresponding to the name.
     */
    public Stash getStash(String name) {
        return stashes.get(name);
    }

    /**
     * Returns whether or not a stash with the given name exists.
     * 
     * @param name The stash name.
     * @return True if a stash with the given name exists, false otherwise.
     */
    public boolean containsStash(String name) {
        return stashes.containsKey(name);
    }

    /**
     * Drops a stash. Does nothing if the stash has already been dropped.
     * 
     * @param name The name of the stash to delete.
     */
    public void dropStash(String name) {
        Stash stash = getStash(name);

        if (stash != null) {
            stashes.remove(name);
            stash.drop();
        }
    }

    /**
     * Loops through each stash's TTL timer wheel and expires any expired keys.
     */
    public void expireTTLKeys() {
        for (Stash stash : stashes.values()) {
            stash.expireTTLKeys();
        }
    }

    /**
     * Returns the number of active stashes.
     * @return The number of active stashes.
     */
    public int getNumStashes() {
        return stashes.size();
    }
}
