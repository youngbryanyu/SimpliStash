package com.youngbryanyu.simplistash.stash.replication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.utils.IOFactory;

/**
 * The replica factory.
 */
@Component
public class ReplicaHandlerFactory {
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
    public ReplicaHandlerFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Creates a replica handler.
     * 
     * @param ip   The ip of the replica.
     * @param port The port of the replica.
     * @return Returns the replica handler.
     */
    public ReplicaHandler createReplica(String ip, int port) {
        IOFactory replicaIOFactory = context.getBean(IOFactory.class);
        return context.getBean(ReplicaHandler.class, replicaIOFactory, ip, port);
    }
}
