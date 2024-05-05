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
     * The maximum number of stashes that can be created.
     */
    private static final int MAX_NUM_STASHES = 100;
    /**
     * The factory used to create new stash instances.
     */
    private final StashFactory stashFactory;
    /**
     * Mapping of each stash's name to its instance. Needs to be a concurrent hash
     * map since we can have multiple NIO worker threads on the server.
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
     * Gets a stash that matches the input name.
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
     * The stash class's methods {@link Stash#get}, {@link Stash#set},
     * {@link Stash#delete}) must guard against race conditions where the stash's DB
     * is being concurrently closed while another thread is attempting to operate on
     * it (or if it has been fully closed already).
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
}
