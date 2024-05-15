package com.youngbryanyu.simplistash.stash;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Service that managers the locks used for concurrent write operations.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LockService {
    
    private static final int DEFAULT_NUM_SEGMENTS = 16;
    private final Lock[] locks;

    // TODO: add javadoc
    @Autowired
    public LockService(int numLocks) { // TODO create factory class or use appconfig.java
        if (numLocks < 0) {
            numLocks = DEFAULT_NUM_SEGMENTS;
        }

        locks = new ReentrantLock[numLocks];
        for (int i = 0; i < numLocks; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    private Lock getLock(Object key) {
        /* Use bit spreading and masking to get segment */
        int lockIndex = (key.hashCode() ^ (key.hashCode() >>> 16)) & (locks.length - 1);
        return locks[lockIndex];
    }

    public void lock(Object key) {
        getLock(key).lock();
    }

    public void unlock(Object key) {
        ReentrantLock lock = (ReentrantLock) getLock(key);
        if (lock.isHeldByCurrentThread()) { // only unlock if held by current thread or else 
            getLock(key).unlock();
        } // java.lang.IllegalMonitorStateException if unlock when not locked
    }
}
