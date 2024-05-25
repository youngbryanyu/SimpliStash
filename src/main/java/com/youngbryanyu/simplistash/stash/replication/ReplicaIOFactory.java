package com.youngbryanyu.simplistash.stash.replication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Replica IO factory.
 */
@Component
public class ReplicaIOFactory {

    /**
     * Constructor.
     */
    @Autowired
    public ReplicaIOFactory() {
    }

    /**
     * Creates a socket.
     * 
     * @param ip   The ip.
     * @param port The port/
     * @return Returns the socket.
     * @throws IOException If an IO exception occurs.
     */
    public Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }

    /**
     * Creates an output writer.
     * 
     * @param socket The socket.
     * @return Returns the socket.
     * @throws IOException If an IO exception occurs.
     */
    public PrintWriter createWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }
}
