package com.youngbryanyu.simplistash.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CLI client used to connect to the database server.
 */
@Component
public class CLIClient {
    /**
     * The socket to connect to the server.
     */
    private Socket socket;
    /**
     * The input reader.
     */
    private BufferedReader in;
    /**
     * The output writer.
     */
    private PrintWriter out;

    /**
     * The constructor.
     */
    @Autowired
    public CLIClient() {
    }

    /**
     * Connects to the server.
     * 
     * @param ip   The server's ip.
     * @param port The server's port.
     * @throws IOException If an I/O exception occurs while connecting to the
     *                     server.
     */
    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Closes the socket and the I/O channels.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    /**
     * Sends a command to the server.
     * 
     * @param command
     */
    public void sendCommand(String command) {
        if (out != null) {
            out.print(command);
            out.flush();
        }
    }

    /**
     * Returns the input stream to read from the server.
     * 
     * @return The input stream.
     */
    public BufferedReader getInputStream() {
        return in;
    }
}
