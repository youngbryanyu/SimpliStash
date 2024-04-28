package com.youngbryanyu.simplistash.stash;

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
     * Creates a new instance of a stash.
     * 
     * @return A stash.
     */
    public Stash createStash() {
        return context.getBean(Stash.class);
    }
}
