package com.youngbryanyu.simplistash.stash.replication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The replica factory.
 */
@Component
public class ReplicaFactory {
    /**
     * The spring application context.
     */
    private ApplicationContext context;

    /**
     * Constructor.
     * 
     * @param context The spring application context.
     */
    @Autowired
    public ReplicaFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Creates a replica handler.
     * 
     * @param ip   The ip of the replica.
     * @param port The port of the replica.
     * @return Returns the replica handler.
     */
    public Replica createReplica(String ip, int port) {
        ReplicaIOFactory replicaIOFactory = context.getBean(ReplicaIOFactory.class);
        return context.getBean(Replica.class, replicaIOFactory, ip, port);
    }
}
