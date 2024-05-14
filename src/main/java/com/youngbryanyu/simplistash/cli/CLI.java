package com.youngbryanyu.simplistash.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;

import com.youngbryanyu.simplistash.cli.commands.CLICommand;
import com.youngbryanyu.simplistash.cli.commands.CLICommandFactory;
import com.youngbryanyu.simplistash.exceptions.BrokenProtocolException;
import com.youngbryanyu.simplistash.exceptions.BufferOverflowException;
import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * The CLI to interact with an instance of SimpliStash.
 */
@Component
public class CLI {
    /**
     * The socket to connect to the server.
     */
    private Socket socket;
    /**
     * The buffered reader to read input from the server.
     */
    private BufferedReader in;
    /**
     * The print writer to write output to the server.
     */
    private PrintWriter out;
    /**
     * The response buffer to get responses from the server.
     */
    private StringBuilder responseBuffer = new StringBuilder();
    /**
     * The tokens parsed from the server.
     */
    private Deque<String> tokens = new LinkedList<>();
    /**
     * The CLI parser.
     */
    private final CommandLineParser parser;
    /**
     * The CLI command factory.
     */
    private final CLICommandFactory cliCommandFactory;

    /**
     * The constructor.
     */
    @Autowired
    public CLI(CLICommandFactory cliCommandFactory) {
        this.cliCommandFactory = cliCommandFactory;
        parser = new DefaultParser();
    }

    /**
     * The main class.
     * 
     * @param args Command line args
     */
    public static void main(String[] args) {
        /* Initialize Spring DI */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CLIConfig.class);

        /* Setup shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            context.close();
        }));

        /* Get the server's IP and port */
        String ip = System.getProperty("ip"); // TODO: move to constructor?
        String port = System.getProperty("port");

        /* Validate IP and port */
        if (ip == null || port == null) {
            System.out.println("Missing required options: ip and port");
            System.out.println("Usage: sstashcli <ip> <port> [command]");
            System.exit(1);
        }

        /* Create an instance of the CLI and start the interactive CLI */
        CLI cli = context.getBean(CLI.class);
        try {
            cli.connect(ip, Integer.parseInt(port));
        } catch (NumberFormatException e) {
            e.printStackTrace(); // TODO handle
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Start the CLI */
        cli.start();
    }

    /**
     * Starts the interactive CLI.
     * 
     * @param ip   The server's IP.
     * @param port The server's port.
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Connected to the server:");

        while (true) {
            System.out.print("> ");
            String userInput = scanner.nextLine();
            if ("exit".equalsIgnoreCase(userInput)) {
                break;
            }

            String response = processCommand(userInput);
            System.out.println(response);
        }

        scanner.close();

        try {
            close();// TODO: move to shutdown hook
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to the server on a socket and sets up I/O.
     * 
     * @throws IOException If an I/O exception occurs when creating the socket.
     */
    private void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Closes the socket and I/O.
     * 
     * @throws IOException If an I/O exception occurs when closing the socket.
     */
    private void close() throws IOException {
        socket.close();
        in.close();
        out.close();
    }

    private String processCommand(String inputLine) {
        String[] args = inputLine.split(" ");
        if (args.length < 1) {
            return "No command provided";
        }

        String commandName = args[0].toUpperCase(); /* Convert to upper case */
        
        try {
            /* Get CLI command */
            CLICommand command = cliCommandFactory.getCommand(commandName);
            
            /* Get command options */
            Options options = command.getOptions();

            /* Parse input line */
            CommandLine commandLine = parser.parse(options, args);

            /* Build encoded message to send to server */
            String encodedCommand = command.encodeCLICommand(commandLine);

            if (encodedCommand == null) {
                return "Usage: " + command.getUsage();
            }

            sendCommand(encodedCommand);

            /* Read response from server */
            try {
                return readAllFromBuffer();
            } catch (IOException e) {
                return "Error reading response: " + e.getMessage();
            }

        } catch (InvalidCommandException e) {
            return "Unknown command: " + commandName;
        } catch (ParseException e) {
            return "Failed to parse command: " + e.getMessage();
        }
    }

    private void sendCommand(String encodedCommand) {
        out.print(encodedCommand);
        out.flush();
    }

    private String readAllFromBuffer() throws IOException {
        while (true) {
            // Read data from the server
            char[] buffer = new char[1024]; // TODO ensure buffer size is 2* max value + max key
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) {
                break;
            }

            // Append the read data to the response buffer
            responseBuffer.append(buffer, 0, bytesRead);

            // Try to parse tokens from the response buffer
            try {
                parseTokens();
            } catch (BrokenProtocolException | BufferOverflowException e) {
                return "Error: " + e.getMessage();
            }

            // Check if we have enough tokens to process a response
            if (tokens.size() >= 2) {
                String responseType = tokens.pollFirst();
                String response = tokens.pollFirst();
                if (responseType.startsWith("VALUE") || responseType.startsWith("ERROR")) {
                    // Wait for the complete response before returning
                    return response;
                } else {
                    tokens.addFirst(response);
                    tokens.addFirst(responseType);
                }
            }
        }
        return responseBuffer.toString();
    }

    private void parseTokens() throws BufferOverflowException, BrokenProtocolException {
        String delim = "\r\n";
        int delimLength = delim.length();
        int lastEndIdx = 0;
        int delimIdx;

        while ((delimIdx = responseBuffer.indexOf(delim, lastEndIdx)) != -1) {
            int size;
            try {
                size = Integer.parseInt(responseBuffer.substring(lastEndIdx, delimIdx));
            } catch (NumberFormatException e) {
                throw new BrokenProtocolException("Invalid token size", e);
            }

            if (size < 1) {
                throw new BrokenProtocolException("Token size out of range", null);
            }

            int startIdx = delimIdx + delimLength;
            int endIdx = startIdx + size;

            if (endIdx > responseBuffer.length()) {
                break;
            }

            String token = responseBuffer.substring(startIdx, endIdx);
            tokens.addLast(token);
            lastEndIdx = endIdx;
        }

        if (lastEndIdx > 0) {
            responseBuffer.delete(0, lastEndIdx);
        }
    }

    private String formatCommand(String input) {
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
        appendToken(sb, currentToken);

        return sb.toString();
    }

    private void appendToken(StringBuilder sb, String token) {
        if (!token.isEmpty()) {
            sb.append(token.length()).append("\r\n").append(token);
        }
    }
}

// TODO: add capability for clicking up and down to get history

// TODO allow quotations marks so users can enter words with spaces

// TODO make extensible for testing