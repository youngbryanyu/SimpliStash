package com.youngbryanyu.simplistash.stash;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.QueueLong.Node.SERIALIZER;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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
     * Creates a new instance of a stash with the given name. 
     * 
     * @param The stash name.
     * @return A stash.
     */
    public Stash createStash(String name) {
        DB db = context.getBean(DB.class);
        HTreeMap<String, String> cache = db.hashMap("primary", SERIALIZER.STRING, SERIALIZER.STRING).create();
        TTLTimeWheel ttlTimeWheel = context.getBean(TTLTimeWheel.class);
        Logger logger = context.getBean(Logger.class);

        return context.getBean(Stash.class, db, cache, ttlTimeWheel, logger, name);
    }
}
