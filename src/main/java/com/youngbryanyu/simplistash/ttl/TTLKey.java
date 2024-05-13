package com.youngbryanyu.simplistash.ttl;

/**
 * Compound key used within each sorted tree map inside each bucket in
 * {@link TTLTimeWheel}.
 */
public class TTLKey implements Comparable<TTLKey> {
    /**
     * The key.
     */
    private final String key;
    /**
     * The expiration time of the key.
     */
    private final long expirationTime;

    /**
     * The constructor.
     * 
     * @param key            The key.
     * @param expirationTime The expiration time of the key.
     */
    public TTLKey(String key, long expirationTime) {
        this.key = key;
        this.expirationTime = expirationTime;
    }

    /**
     * Return's the key.
     * 
     * @return The key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the key's expiration time.
     * 
     * @return The key's expiration time.
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Method for comparing with another TTL key. Compare first by expiration time,
     * then by key.
     * 
     * @param other The other TTL key.
     * @return Returns -1 if the current key is less, 0 if the same, 1 if greater.
     */
    @Override
    public int compareTo(TTLKey other) {
        int result = Long.compare(expirationTime, other.expirationTime);
        if (result == 0) {
            return key.compareTo(other.key);
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("key=%s,exp=%d", key, expirationTime);
    }
}
