package com.youngbryanyu.simplistash.stash;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Class representing a stash which serves as a single table of key-value pairs
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Stash {
    /**
     * The max key size allowed in the stash.
     */
    public static final int MAX_KEY_SIZE = 256;
    /**
     * The max value size allowed in the stash.
     */
    public static final int MAX_VALUE_SIZE = 65536;
    /**
     * The max size of a stash's name.
     */
    public static final int MAX_NAME_SIZE = 64;
    /**
     * Name of the primary cache.
     */
    private static final String PRIMARY_CACHE_NAME = "primary";
    /**
     * Error message when attempting to access a closed DB.
     */
    private static final String DB_CLOSED_ERROR = "The specified stash doesn't exist";
    /**
     * A single DB store instance tied to the stash.
     */
    private DB db;
    /**
     * The primary cache provide O(1) direct access to values by key.
     */
    private HTreeMap<String, String> cache;
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the stash.
     * 
     * @param db     The DB instance.
     * @param logger The application logger.
     */
    @Autowired
    public Stash(DB db, Logger logger) {
        this.db = db;
        this.logger = logger;
        createPrimaryCache();
    }

    /**
     * Creates the primary cache for O(1) access to fields directly.
     */
    private void createPrimaryCache() {
        cache = db.hashMap(PRIMARY_CACHE_NAME, Serializer.STRING, Serializer.STRING).create();
    }

    /**
     * Sets a key value pair in the stash. Overwrites existing pairs. Returns the OK
     * response. Returns an error message if the DB is being closed or has already
     * been closed by another concurrent client.
     * 
     * @param key   The unique key.
     * @param value The value to map to the key.
     */
    public String set(String key, String value) {
        try {
            cache.put(key, value);
        } catch (NullPointerException e) {
            /*
             * The below exception can be thrown when the DB is being closed by another
             * thread:
             * 
             * java.lang.NullPointerException: Cannot read the array length because "slices"
             * is null
             */
            logger.debug("Stash set failed, stash doesn't exist (NullPointerException)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        } catch (IllegalAccessError e) {
            /*
             * The below exception can be thrown when the DB has been closed by another
             * thread:
             * 
             * java.lang.IllegalAccessError: Store was closed
             */
            logger.debug("Stash set failed, stash doesn't exist (IllegalAccessError)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        }

        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Retrieves a value from the stash matching the key. Returns an error message
     * if the DB is being closed or has already been closed by another concurrent
     * client.
     * 
     * @param key The key of the value to get.
     * @return The value matching the key.
     */
    public String get(String key) {
        try {
            return cache.get(key);
        } catch (NullPointerException e) {
            /*
             * The below exception can be thrown when the DB is being closed by another
             * thread:
             * 
             * java.lang.NullPointerException: Cannot read the array length because "slices"
             * is null
             */
            logger.debug("Stash get failed, stash doesn't exist (NullPointerException)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        } catch (IllegalAccessError e) {
            /*
             * The below exception can be thrown when the DB has been closed by another
             * thread:
             * 
             * java.lang.IllegalAccessError: Store was closed
             */
            logger.debug("Stash get failed, stash doesn't exist (IllegalAccessError)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        }
    }

    /**
     * Deletes a key from the stash. Returns the OK response. Returns an error
     * message if the DB is being closed or has already been closed by another
     * concurrent client.
     * 
     * @param key The key to delete.
     */
    public String delete(String key) {
        try {
            cache.remove(key);
        } catch (NullPointerException e) {
            /*
             * The below exception can be thrown when the DB is being closed by another
             * thread:
             * 
             * java.lang.NullPointerException: Cannot read the array length because "slices"
             * is null
             */
            logger.debug("Stash delete failed, stash doesn't exist (NullPointerException)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        } catch (IllegalAccessError e) {
            /*
             * The below exception can be thrown when the DB has been closed by another
             * thread:
             * 
             * java.lang.IllegalAccessError: Store was closed
             */
            logger.debug("Stash delete failed, stash doesn't exist (IllegalAccessError)");
            return ProtocolUtil.buildErrorResponse(DB_CLOSED_ERROR);
        }

        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Closes the stash by closing its DB.
     */
    public void drop() {
        db.close();
    }
}
