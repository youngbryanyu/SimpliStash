package com.youngbryanyu.simplistash.stash;

import java.util.concurrent.ConcurrentHashMap;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.QueueLong.Node.SERIALIZER;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.ttl.TTLTimeWheel;

/**
 * The stash factory used to create stash objects.
 */
@Component
public class StashFactory {
    /**
     * The spring IoC container holding all beans.
     */
    private final ApplicationContext context;

    /**
     * Constructor for the stash factory.
     * 
     * @param context The spring IoC container.
     */
    @Autowired
    public StashFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Creates a new instance of an off-heap stash with the given name. 
     * 
     * @param The stash name.
     * @return A stash.
     */
    public OffHeapStash createOffHeapStash(String name) {
        DB db = context.getBean(DB.class);
        HTreeMap<String, String> cache = db.hashMap("primary", SERIALIZER.STRING, SERIALIZER.STRING).create();
        TTLTimeWheel ttlTimeWheel = context.getBean(TTLTimeWheel.class);
        Logger logger = context.getBean(Logger.class);

        return context.getBean(OffHeapStash.class, db, cache, ttlTimeWheel, logger, name);
    }

   /**
     * Creates a new instance of an on-heap stash with the given name. 
     * 
     * @param The stash name.
     * @return A stash.
     */
    public Stash createOnHeapStash(String name) {
        ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
        TTLTimeWheel ttlTimeWheel = context.getBean(TTLTimeWheel.class);
        Logger logger = context.getBean(Logger.class);

        return context.getBean(OnHeapStash.class, cache, ttlTimeWheel, logger, name);
    }
}
