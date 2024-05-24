package com.youngbryanyu.simplistash.ttl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The TTL data structure used to manage expiration of TTLed keys. Doesn't have
 * access to the actual data stored in memory, but keeps track of keys that are
 * currently TTLed.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TTLTimeWheel {
    /**
     * The bucket window size for each bucket in the time wheel, in milliseconds.
     */
    public static final int BUCKET_WINDOW_SIZE = 60_000;
    /**
     * The number of buckets in the TTL time wheel.
     */
    private static final int NUM_BUCKETS = 1440;
    /**
     * The max number of entries to expire in 1 expiration operation.
     */
    public static final int MAX_EXPIRE_LIMIT = 100;
    /**
     * Map of keys to their compound ttl key consisting of (key, expirationTime).
     * Only 1 thread handles writes so no need to make thread-safe yet.
     */
    private final Map<String, TTLKey> ttlMap;
    /**
     * Buckets containing a tree set of compound ttl keys consisting of (key,
     * expirationTime).
     */
    private final TreeSet<TTLKey>[] buckets;
    /**
     * The current bucket to try expiring keys from.
     */
    private int currentBucketIndex;

    /**
     * Constructor for the TTL time wheel.
     */
    @Autowired
    @SuppressWarnings("unchecked")
    public TTLTimeWheel() {
        ttlMap = new HashMap<>();
        buckets = new TreeSet[NUM_BUCKETS];
        currentBucketIndex = 0;
    }

    /**
     * Calculates the index of the bucket that a particular expiration time
     * corresponds to. The expiration time is calculated as milliseconds from epoch
     * UTC.
     * 
     * @param expirationTime The expiration time in milliseconds since epoch UTC.
     * @return The bucket index corresponding to the expiration time.
     */
    private int getBucketIndex(long expirationTime) {
        long ticksFromEpoch = expirationTime / BUCKET_WINDOW_SIZE;
        return (int) (ticksFromEpoch % NUM_BUCKETS);
    }

    /**
     * Adds a key to the TTL time wheel. Removes the existing key's TTL if it
     * already had one, so this functions as an update method as well.
     * 
     * @param key They key.
     * @param ttl The ttl.
     */
    public void add(String key, long ttl) {
        /* Delete the key if it already exists */
        remove(key);

        long expirationTime = System.currentTimeMillis() + ttl;
        TTLKey ttlKey = new TTLKey(key, expirationTime);

        /* 1. Insert into key expiration map */
        ttlMap.put(key, ttlKey);

        /* Get bucket */
        int bucketIndex = getBucketIndex(expirationTime);
        if (buckets[bucketIndex] == null) { /* Lazy initialize bucket */
            buckets[bucketIndex] = new TreeSet<>(); /* Use TTLKey class's comparator */
        }

        /* 2. Insert into bucket */
        buckets[bucketIndex].add(ttlKey);
    }

    /**
     * Removes a key from the TTL time wheel data structure. Does nothing if the key
     * doesn't exist.
     * 
     * @param key The key.
     */
    public void remove(String key) {
        if (!ttlMap.containsKey(key)) {
            return;
        }

        TTLKey ttlKey = ttlMap.get(key);
        long expirationTime = ttlKey.getExpirationTime();

        /* Get bucket */
        int bucketIndex = getBucketIndex(expirationTime);

        // If a timestamp's bucket is non-null, its key must exist in the map, thus we
        // don't check if `bucket[bucketIndex] == null` since it is redundant.

        /* 1. Remove from bucket */
        buckets[bucketIndex].remove(ttlKey);

        /* 2. Remove from key expiration map */
        ttlMap.remove(key);
    }

    /**
     * Returns whether or not a key is expired.
     * 
     * @param key The key.
     * @return True if the key has expired, false if it has not or does not have a
     *         TTL.
     */
    public boolean isExpired(String key) {
        if (!ttlMap.containsKey(key)) {
            return false;
        }

        return System.currentTimeMillis() >= ttlMap.get(key).getExpirationTime();
    }

    /**
     * Expires a batch of keys up to the max expire limit.
     * 
     * @return The list of keys that were expired.
     */
    public List<String> expireKeys() {
        List<String> expiredKeys = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        int numExpired = 0;
        int startBucketIndex = currentBucketIndex;

        /* Expire up to the max expire limit */
        while (!ttlMap.isEmpty() && numExpired < MAX_EXPIRE_LIMIT) {
            TreeSet<TTLKey> bucket = buckets[currentBucketIndex];

            if (bucket != null) {
                while (!bucket.isEmpty()) {
                    TTLKey ttlKey = bucket.first();

                    /* No more expired keys from current bucket so break */
                    if (ttlKey.getExpirationTime() > currentTime) {
                        break;
                    }

                    /* Add to expired key list and remove the key */
                    expiredKeys.add(ttlKey.getKey());
                    remove(ttlKey.getKey());
                    numExpired++;

                    /* Break if we've reached the expire limit */
                    if (numExpired >= MAX_EXPIRE_LIMIT) {
                        break;
                    }
                }
            }

            /* Go to next bucket */
            currentBucketIndex++;
            if (currentBucketIndex >= buckets.length) {
                currentBucketIndex = 0;
            }

            /* If we've iterated over every bucket and there's no new TTLs, break */
            if (currentBucketIndex == startBucketIndex) {
                break;
            }
        }

        return expiredKeys;
    }

    /**
     * Clears all values from the TTL time wheel.
     */
    public void clear() {
        /* Clear TTL map */
        ttlMap.clear();

        /* Clear all buckets */
        for (TreeSet<TTLKey> bucket : buckets) {
            if (bucket != null) {
                bucket.clear();
            }
        }
    }
}
