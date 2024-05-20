package com.youngbryanyu.simplistash.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.stereotype.Component;

/**
 * The CLI client factory.
 */
@Component
public class CLIClientFactory {
    /**
     * Creates a socket connection.
     * 
     * @param ip   The server IP.
     * @param port The server port.
     * @return A socket.
     * @throws IOException If an IO exceptions occurs.
     */
    public Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }

    /**
     * Creates a buffered reader.
     * 
     * @param socket The socket used to connect to the server.
     * @return The buffered reader.
     * @throws IOException If an IO exceptions occurs.
     */
    public BufferedReader createBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Creates a print writer.
     * 
     * @param socket The socket used to connect to the server.
     * @return The print writer.
     * @throws IOException If an IO exceptions occurs.
     */
    public PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }
}
