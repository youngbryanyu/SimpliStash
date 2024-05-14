package com.youngbryanyu.simplistash.cli;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * The CLI
 */
public class CLI {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public static void main(String[] args) {
        String ipAddress = System.getProperty("ip");
        String portNumber = System.getProperty("port");

        if (ipAddress == null || portNumber == null) {
            System.out.println("Missing required options: ip and port");
            System.out.println("Usage: sstashcli -i <IP> -p <Port>");
            System.exit(1);
        }

        try {
            new CLI().start(ipAddress, Integer.parseInt(portNumber));
        } catch (NumberFormatException e) {
            System.out.println("Port number must be an integer");
            System.exit(1);
        }
    }

    public void start(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Connected to the server. Enter your commands:");

            while (true) {
                String userInput = scanner.nextLine();
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }

                // Process the user input and send to the server
                String formattedCommand = formatCommand(userInput);
                out.println(formattedCommand);

                // Read response from the server
                String response = in.readLine();
                System.out.println("Response from server: " + response);
            }

            socket.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatCommand(String input) {
        // Tokenize the input while handling quotes
        StringTokenizer tokenizer = new StringTokenizer(input, "\"\' ", true);
        StringBuilder sb = new StringBuilder();
        boolean insideQuote = false;
        String currentToken = "";

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("\"".equals(token) || "\'".equals(token)) {
                insideQuote = !insideQuote;
            } else if (" ".equals(token)) {
                if (insideQuote) {
                    currentToken += token;
                } else {
                    appendToken(sb, currentToken);
                    currentToken = "";
                }
            } else {
                currentToken += token;
            }
        }
        appendToken(sb, currentToken); // Append the last token

        return sb.toString();
    }

    private void appendToken(StringBuilder sb, String token) {
        if (!token.isEmpty()) {
            sb.append(token.length()).append("\r\n").append(token);
        }
    }
}
