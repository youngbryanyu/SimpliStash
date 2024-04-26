package com.youngbryanyu.simplistash.stash;

/**
 * Class representing a "stash" which serves as a single table of key-value pairs
 */
public class Stash {
    /**
     * The max key size allowed in the stash
     */
    private static final int MAX_KEY_SIZE = 256;
    /**
     * The max value size allowed in the stash
     */
    private static final int MAX_VALUE_SIZE = 65536;

    public void set(String key, String value) {

    }
    
    public static int getMaxKeySize() {
        return MAX_KEY_SIZE;
    }

    public static int getMaxValueSize() {
        return MAX_VALUE_SIZE;
    }
}
