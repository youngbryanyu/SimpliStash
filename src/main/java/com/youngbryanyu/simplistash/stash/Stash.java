package com.youngbryanyu.simplistash.stash;

/**
 * The stash interface.
 */
public interface Stash {
     /**
     * The max key length allowed in the stash.
     */
    public static final int MAX_KEY_LENGTH = 256;
    /**
     * The max value length allowed in the stash.
     */
    public static final int MAX_VALUE_LENGTH = 65536;
    /**
     * The max alloed length of a stash's name.
     */
    public static final int MAX_NAME_LENGTH = 64;
    /**
     * Error message when attempting to access a closed DB.
     */
    public static final String DB_CLOSED_ERROR = "The specified stash doesn't exist.";

    // TODO: add javadoc
    public void set(String key, String value);

    public String get(String key, boolean readOnly);

    public boolean contains(String key, boolean readOnly);

    public void delete(String key);

    public void setWithTTL(String key, String value, long ttl);

    public boolean updateTTL(String key, long ttl);

    public void drop();

    public void expireTTLKeys();

    public String getInfo();
}
