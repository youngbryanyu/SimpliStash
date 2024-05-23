package com.youngbryanyu.simplistash.cli.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cli.CLI;
import com.youngbryanyu.simplistash.cli.CLIClient;
import com.youngbryanyu.simplistash.commands.read.PingCommand;
import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;

/**
 * The CLI processor that processes CLI commands, sends them to the server, and
 * gets the response.
 */
@Component
public class CLICommandHandler {
    /**
     * The CLI command factory.
     */
    private final CLICommandFactory cliCommandFactory;
    /**
     * The command line parser.
     */
    private final CommandLineParser parser;
    /**
     * The CLI client.
     */
    private final CLIClient cliClient;

    /**
     * The constructor.
     * 
     * @param cliCommandFactory The CLI commmand factory.
     * @param cliClient         The CLI client.
     */
    @Autowired
    public CLICommandHandler(CLICommandFactory cliCommandFactory, CLIClient cliClient, CommandLineParser parser) {
        this.cliCommandFactory = cliCommandFactory;
        this.cliClient = cliClient;
        this.parser = parser;
    }

    /**
     * Processes a command from an input line read from the CLI terminal.
     * 
     * @param inputLine The input line.
     * @return Returns the response to display in the CLI, or null to indicate do
     *         nothing.
     */
    public String processCommand(String inputLine) {
        /* Parse args from input line */
        String[] args = parseArgsFromCLI(inputLine);

        /* Check if any command was sent at all */
        if (args.length < 1) {
            return null;
        }

        /* Send PING, exit if server disconnected */
        cliClient.sendCommand(ProtocolUtil.encode(PingCommand.NAME));
        try {
            if (!readAllFromBuffer().equals(ProtocolUtil.PONG_RESPONSE)) {
                return CLI.EXIT;
            }
        } catch (IOException e) {
            return CLI.EXIT;
        }

        /* Get command name in upper case */
        String commandName = args[0].toUpperCase();

        try {
            /* Get command */
            CLICommand command = cliCommandFactory.getCommand(commandName);

            /* Get command's options (optional args) */
            Options options = command.getOptions();

            /* Parse args into command line object based on options */
            CommandLine commandLine = parser.parse(options, args);

            /* Encode the command line object into the server protocol */
            String encodedCommand = command.encodeCLICommand(commandLine);

            /* Check if enough args were specified for the command */
            if (encodedCommand == null) {
                return "Usage: " + command.getUsage();
            }

            /* Send command to server */
            cliClient.sendCommand(encodedCommand);

            /* Read response from server */
            return readAllFromBuffer();
        } catch (InvalidCommandException e) { /* Handle invalid command */
            return "Unknown command: " + commandName;
        } catch (ParseException e) { /* Handle parse exception */
            return "Failed to process command. " + e.getMessage();
        } catch (IOException e) {
            return CLI.EXIT; /* Exit if server disconnected */
        }
    }

    /**
     * Reads input from the server and processes it into the buffer then token
     * deque.
     * 
     * @return A string containing everything read from the server.
     * @throws IOException If the read operation throws an I/O exception.
     */
    protected String readAllFromBuffer() throws IOException {
        BufferedReader in = cliClient.getInputStream(); /* Get client's input stream */
        StringBuilder buffer = new StringBuilder();
        Deque<String> tokens = new LinkedList<>();

        while (true) {
            /* Read from input stream into buf */
            char[] buf = new char[Stash.MAX_KEY_LENGTH + Stash.MAX_VALUE_LENGTH];
            int bytesRead = in.read(buf);
            if (bytesRead == -1) {
                break; // if server sent EOF
            }

            /* Add output to buffer */
            buffer.append(buf, 0, bytesRead);

            /* Parse tokens */
            parseTokensFromServer(buffer, tokens);

            /* Check if we have a full response: <response_type> <response> */
            if (tokens.size() >= 2) {
                String responseType = tokens.pollFirst();
                String response = tokens.pollFirst();

                /* Handle fatal error edge case (protocol issues) */
                if (responseType.startsWith(ProtocolUtil.FATAL_PREFIX)) {
                    System.out.println(response);
                    return CLI.EXIT;
                }

                /* Return value or error message */
                return response;
            }
        }

        return buffer.toString();
    }

    /**
     * Parse tokens from the server stored in the buffer into the deque. We assume
     * that the server follows the protocol and don't perform any protocol checks
     * such as checking if the length-prefix is a valid number.
     * 
     * @param buffer The buffer of data read from the server.
     * @param tokens The parsed tokens.
     */
    protected void parseTokensFromServer(StringBuilder buffer, Deque<String> tokens) {
        String delim = ProtocolUtil.DELIM;
        int delimLength = delim.length();
        int lastEndIdx = 0; /* End of final token parsed */
        int delimIdx;

        while ((delimIdx = buffer.indexOf(delim, lastEndIdx)) != -1) {
            /* Get token size */
            int size = Integer.parseInt(buffer.substring(lastEndIdx, delimIdx));

            /* Get start and end indices of token */
            int startIdx = delimIdx + delimLength;
            int endIdx = startIdx + size;

            /* Check if buffer contains enough bytes */
            if (endIdx > buffer.length()) {
                break;
            }

            /* Add token to deque */
            String token = buffer.substring(startIdx, endIdx);
            tokens.addLast(token);
            lastEndIdx = endIdx;
        }

        /* Delete processed tokens from buffer */
        if (lastEndIdx > 0) {
            buffer.delete(0, lastEndIdx);
        }
    }

    /**
     * Parses space separated arguments from string input from the CLI terminal.
     * Ensures that arguments inside double quotes are treated as one argument.
     * 
     * @param input The input to parse into arguments.
     * @return The arguments parsed from the input.
     */
    protected String[] parseArgsFromCLI(String input) {
        List<String> arguments = new ArrayList<>();

        /* Use regex to match arguments */
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                /* Quoted argument, but without the quotes */
                arguments.add(matcher.group(1));
            } else {
                /* Non-quoted argument */
                arguments.add(matcher.group(2));
            }
        }

        return arguments.toArray(new String[arguments.size()]);
    }
}
