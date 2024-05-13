package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the server monitor.
 */
public class ServerMonitorTest {
    /**
     * Test that the initial server crashed state is not crashed.
     */
    @Test
    void testInitialServerCrashedState() {
        ServerMonitor monitor = new ServerMonitor();
        assertFalse(monitor.didServerCrash());
    }

    /**
     * Test {@link ServerMonitor#setServerCrashed()}.
     */
    @Test
    void testSetServerCrashed() {
        ServerMonitor monitor = new ServerMonitor();
        monitor.setServerCrashed();
        assertTrue(monitor.didServerCrash());
    }

    /**
     * Test {@link ServerMonitor#waitForCrash()}.
     */
    @Test
    void testWaitForCrashUnblocksAfterCrash() throws InterruptedException {
        ServerMonitor monitor = new ServerMonitor();
        Thread testThread = new Thread(() -> {
            try {
                monitor.waitForCrash();
                assertFalse(monitor.didServerCrash());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); /* Handle thread interruption properly */
            }
        });

        /* Start the test thread and then ensure it's waiting */
        testThread.start();
        Thread.sleep(1000); /*  Allow some time for the thread to start and possibly block */
        assertFalse(testThread.getState() == Thread.State.TERMINATED);

        /* Set the server to crashed */
        monitor.setServerCrashed();

        /* Wait for a little while to allow thread to respond */
        testThread.join(1000); /*  Wait for the thread to finish execution */
        assertFalse(testThread.isAlive());
    }
}
