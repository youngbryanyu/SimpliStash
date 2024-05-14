package com.youngbryanyu.simplistash.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cli.commands.CLICommandHandler;

import java.io.IOException;

/**
 * The CLI class.
 */
@Component
public class CLI {
    /**
     * The command that signals an exit from the CLI.
     */
    public static final String EXIT = "exit";
    /**
     * The CLI command handler.
     */
    private final CLICommandHandler cliCommandHandler;
    /**
     * The CLI client.
     */
    private final CLIClient cliClient;
    /**
     * Terminal handler.
     */
    private final TerminalHandler terminalHandler;

    /**
     * The constructor.
     * 
     * @param cliCommandHandler The CLI command handler.
     * @param cliClient         The CLI client.
     * @param terminalHandler   The terminal handler.
     */
    @Autowired
    public CLI(CLICommandHandler cliCommandHandler, CLIClient cliClient, TerminalHandler terminalHandler) {
        this.cliCommandHandler = cliCommandHandler;
        this.cliClient = cliClient;
        this.terminalHandler = terminalHandler;
    }

    /**
     * Starts the CLI.
     */
    public void start() {
        System.out.println("Connected to the server:");

        while (true) {
            /* Read line from client */
            String userInput = terminalHandler.readLine("> ");

            /* Handle exit case */
            if (userInput.equalsIgnoreCase(EXIT)) {
                break;
            }

            /* Process command and show response */
            String response = cliCommandHandler.processCommand(userInput);
            if (response != null) {
                if (response.equals(EXIT)) {
                    System.out.println("Server disconnected, exiting...");
                    break;
                }
                System.out.println(response);
            }
        }

        try {
            cliClient.close();
        } catch (IOException e) {
            System.out.println("Failed to gracefully close the CLI client: " + e.getMessage());
        }
    }

    /**
     * Main method to run the CLI.
     * 
     * @param args Command line args.
     */
    public static void main(String[] args) {
        /* Initialize spring IoC container */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CLIConfig.class);

        /* Create shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> context.close()));

        /* Get port and ip of server */
        String ip = System.getProperty("ip");
        String port = System.getProperty("port");

        /* Validate port and ip */
        if (ip == null || port == null) {
            System.out.println("Missing required options: ip and port");
            System.out.println("Usage: sstashcli <ip> <port> [command]");
            return;
        }

        /* Start CLI and connect to server */
        CLI cli = context.getBean(CLI.class);
        try {
            cli.cliClient.connect(ip, Integer.parseInt(port));
            cli.start();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }
}
