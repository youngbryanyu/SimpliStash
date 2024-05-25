package com.youngbryanyu.simplistash.server.primary;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class representing a read replica to forward commands to.
 */
public class Replica {
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
     * The constructor.
     * 
     * @param ip   The replica's ip.
     * @param port The replica's port.
     */
    public Replica(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Connects to the replica.
     */
    public void connect() {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
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
            System.out.println("Forwarded command:");
            System.out.println(encodedCommand);
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
            e.printStackTrace();
        }
    }
}
