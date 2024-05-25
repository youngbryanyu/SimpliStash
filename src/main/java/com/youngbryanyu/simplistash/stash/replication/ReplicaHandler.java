package com.youngbryanyu.simplistash.stash.replication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.utils.IOFactory;

/**
 * Class representing a read replica to forward commands to.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReplicaHandler {
    /**
     * The replica's ip.
     */
    private final String ip;
    /**
     * The replica's port.
     */
    private final int port;
    /**
     * The socket.
     */
    private Socket socket;
    /**
     * The output writer.
     */
    private PrintWriter out;
    /**
     * The replica IO factory.
     */
    private final IOFactory replicaIOFactory;

    /**
     * The constructor.
     * 
     * @param ip   The replica's ip.
     * @param port The replica's port.
     */
    @Autowired
    public ReplicaHandler(IOFactory replicaIOFactory, String ip, int port) {
        this.replicaIOFactory = replicaIOFactory;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Connects to the replica.
     */
    public void connect() {
        try {
            socket = replicaIOFactory.createSocket(ip, port);
            out = replicaIOFactory.createWriter(socket);
        } catch (IOException e) {
            System.out.println("Failed to establish replica connection: " + e.getMessage());
        }
    }

    /**
     * Forwards a command to a replica.
     * 
     * @param encodedCommand The encoded command.
     */
    public void forwardCommand(String encodedCommand) {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            connect();
        }
        if (out != null) {
            out.print(encodedCommand);
            out.flush();
        }
    }

    /**
     * Closes the connection to the replica.
     */
    public void close() {
        try {
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to gracefully close replica connection: " + e.getMessage());
        }
    }

    /**
     * Returns the socket.
     * 
     * @return The socket.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Returns the print writer.
     * 
     * @return The print writer.
     */
    public PrintWriter getWriter() {
        return out;
    }
}
