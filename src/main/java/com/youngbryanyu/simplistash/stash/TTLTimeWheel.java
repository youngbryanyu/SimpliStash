package com.youngbryanyu.simplistash.stash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    private static final int BUCKET_WINDOW_SIZE = 60_000;
    /**
     * The number of buckets in the TTL time wheel.
     */
    private static final int NUM_BUCKETS = 1440;
    /**
     * The max number of entries to expire in 1 expiration operation.
     */
    private static final int MAX_EXPIRE_LIMIT = 100;
    /**
     * Map of keys to their expiration times.
     */
    private final Map<String, Long> ttlMap;
    /**
     * Buckets containing a Tree Map of keys to their expiration times sorted by
     * expiration time.
     * 
     * Because the comparator of the tree map in `buckets` depends on the hash map
     * `ttlMap`, we must perform operations in the following order:
     * - Keys must be inserted into `ttMap` before `buckets`
     * - Keys must be removed from `buckets` before `ttlMap`
     */
    private final TreeMap<String, Long>[] buckets;
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
        buckets = new TreeMap[NUM_BUCKETS];
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
     * already had one.
     * 
     * @param key They key.
     * @param ttl The ttl.
     */
    public void add(String key, long ttl) {
        /* Delete the key if it already exists */
        remove(key);

        long expirationTime = System.currentTimeMillis() + ttl;

        /* 1. Insert into key expiration map */
        ttlMap.put(key, expirationTime);

        /* Get bucket */
        int bucketIndex = getBucketIndex(expirationTime);
        if (buckets[bucketIndex] == null) { /* Lazy initialize bucket */
            buckets[bucketIndex] = new TreeMap<>((k1, k2) -> Long.compare(ttlMap.get(k1), ttlMap.get(k2)));
        }

        /* 2. Insert into bucket */
        buckets[bucketIndex].put(key, expirationTime);
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

        long expirationTime = ttlMap.get(key);

        /* Get bucket */
        int bucketIndex = getBucketIndex(expirationTime);
        if (buckets[bucketIndex] == null) {
            return;
        }

        /* 1. Remove from bucket */
        buckets[bucketIndex].remove(key);

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

        return System.currentTimeMillis() >= ttlMap.get(key);
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
            TreeMap<String, Long> bucket = buckets[currentBucketIndex];

            if (bucket != null) {
                while (!bucket.isEmpty()) {
                    Map.Entry<String, Long> entry = bucket.firstEntry();

                    /* No more expired keys from current bucket so break */
                    if (entry.getValue() > currentTime) {
                        break;
                    }

                    /* Add to expired key list and remove the key */
                    expiredKeys.add(entry.getKey());
                    remove(entry.getKey());
                    bucket.pollFirstEntry();
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
}
