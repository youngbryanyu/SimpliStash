package com.youngbryanyu.simplistash.server.primary;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Class that handles periodic expiration of TTLed keys.
 */
@Component
public class KeyExpirationManager {
    /**
     * The TTL active expiration delay in seconds.
     */
    private static final int TTL_EXPIRE_DELAY = 1;
    /**
     * The scheduled future of the task to expire keys
     */
    private ScheduledFuture<?> expireTask;
    /**
     * The stash manager.
     */
    private StashManager stashManager;

    /**
     * Constructor for the key expiration manager.
     */
    @Autowired
    public KeyExpirationManager(StashManager stashManager) {
        this.stashManager = stashManager;
    }

    /**
     * Starts the scheduled expiration task. Does nothing if there is a task already
     * scheduled.
     * 
     * @param group        The event loop group to run the task in.
     * @param stashManager The stash manager.
     */
    public void startExpirationTask(EventLoopGroup group) {
        if (expireTask == null || expireTask.isDone()) {
            expireTask = group.scheduleWithFixedDelay(() -> {
                stashManager.expireTTLKeys();
            }, 0, TTL_EXPIRE_DELAY, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the scheduled expiration task.
     */
    public void stopExpirationTask() {
        if (expireTask != null && !expireTask.isCancelled()) {
            expireTask.cancel(false);
        }
    }
}
