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

    public Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }

    public BufferedReader createBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }
}
