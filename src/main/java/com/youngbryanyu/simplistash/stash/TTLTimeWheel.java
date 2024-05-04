package com.youngbryanyu.simplistash.stash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The TTL data structure used to manage active expiration of TTLed keys.
 * Doesn't have access to the actual data stored in memory, but keeps track of
 * keys that are currently TTLed.
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
    private final List<TreeMap<String, Long>> buckets;
    /**
     * The current bucket to try expiring keys from. The current bucket will go on
     * to the next if the current bucket is empty and there are more keys to expire.
     */
    private int currentBucketIndex;
    /**
     * The application logger
     */
    private final Logger logger;

    /**
     * Constructor for the TTL time wheel.
     */
    @Autowired
    public TTLTimeWheel(Logger logger) {
        this.logger = logger;
        ttlMap = new HashMap<>();
        buckets = new ArrayList<>();

        for (int i = 0; i < NUM_BUCKETS; i++) {
            buckets.add(new TreeMap<>((k1, k2) -> Long.compare(ttlMap.get(k1), ttlMap.get(k2))));
        }

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

    public void add(String key, long ttl) {
        /* Delete the key if it already exists */
        remove(key);

        long expirationTime = System.currentTimeMillis() - ttl;

        /* 1. Insert into key expiration map */
        ttlMap.put(key, expirationTime);

        /* 2. Insert into buckets */
        int bucketIndex = getBucketIndex(expirationTime);
        buckets.get(bucketIndex).put(key, expirationTime);
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

        /* 1. Remove from buckets */
        int bucketIndex = getBucketIndex(expirationTime);
        buckets.get(bucketIndex).remove(key);

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

        return System.currentTimeMillis() >= ttlMap.get(key) ;
    }

    public List<String> expireKeys() {
        List<String> expiredKeys = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        int numExpired = 0;

        /* Expire up to the max expire limit */
        while (!ttlMap.isEmpty() && numExpired < MAX_EXPIRE_LIMIT) {
            TreeMap<String, Long> bucket = buckets.get(currentBucketIndex);

            while (!bucket.isEmpty()) {
                Map.Entry<String, Long> entry = bucket.firstEntry();

                /* No more expired keys from current bucket so break */
                if (entry.getValue() < currentTime) {
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

            /* Go to next bucket */
            currentBucketIndex++;
            if (currentBucketIndex >= buckets.size()) {
                currentBucketIndex = 0;
            }
        }
        return expiredKeys;
    }
}
