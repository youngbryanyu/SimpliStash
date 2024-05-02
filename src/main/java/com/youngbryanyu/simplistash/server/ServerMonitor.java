package com.youngbryanyu.simplistash.server;

import org.springframework.stereotype.Component;

/**
 * Class to help monitor whether one of the servers crashed.
 */
@Component
public class ServerMonitor {
    /**
     * Whether or not any of the servers crashed.
     */
    private boolean serverCrashed;

    /**
     * Constructor for the server monitor.
     */
    public ServerMonitor() {
        serverCrashed = false;
    }

    /**
     * Sets the server crashed flag to true indicating one of the servers crashed.
     */
    public synchronized void setServerCrashed() {
        this.serverCrashed = true;
        notifyAll(); /* Notify any waiting threads */
    }

    /**
     * Waits and blocks until an error occurs.
     * 
     * @throws InterruptedException If the thead is interrupted.
     */
    public synchronized void waitForCrash() throws InterruptedException {
        while (!serverCrashed) {
            wait(); /* Wait until an error occurs or a notification is sent */
        }
    }

    /**
     * Returns whether or not a server has crashed.
     * 
     * @return True if one of the servers crashed, false otherwise.
     */
    public synchronized boolean didServerCrashed() {
        return serverCrashed;
    }
}
