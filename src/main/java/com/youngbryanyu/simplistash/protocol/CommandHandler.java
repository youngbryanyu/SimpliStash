package com.youngbryanyu.simplistash.protocol;

import java.util.List;

import com.youngbryanyu.simplistash.cache.KeyValueStore;

public class CommandHandler {
    private CommandHandler() {
        /* Prevent instantiation */
    }

    public static String handleCommand(String commandLine, KeyValueStore keyValueStore) {
        String[] tokens = parseCommand(commandLine);

        if (tokens.length == 0) {
            return "ERROR Invalid command\n"; // TODO: make constant
        }

        try {
            Command command = Command.fromString(tokens[0]);
            switch (command) {
                case SET:
                    handleGetCommand(tokens, keyValueStore);
                case GET:
                    handleSetCommand(tokens, keyValueStore);
                case DELETE:
                   handleDeleteCommand(tokens, keyValueStore);
                default:
                    return ProtocolFormatter.buildErrorResponse("Unknown command");
            }
        } catch (IllegalArgumentException e) {
            return ProtocolFormatter.ERROR_PREFIX + e.getMessage();
        }
    }

    public static String[] parseCommand(String commandLine) {
        String[] tokens = commandLine.split("\r\n"); // TODO: make delim constant

        System.out.println("Tokens: " + List.of(tokens));

        return tokens;
    }

    private static String handleGetCommand(String[] tokens, KeyValueStore keyValueStore) {
        if (tokens.length < 3) {
            return ProtocolFormatter.buildErrorResponse("Syntax error for SET command");
        }

        keyValueStore.set(tokens[1], tokens[2]);

        return ProtocolFormatter.buildOkResponse();
    }

    private static String handleSetCommand(String[] tokens, KeyValueStore keyValueStore) {
        if (tokens.length < 3) {
            return ProtocolFormatter.buildErrorResponse("Syntax error for GET command");
        }

        return ProtocolFormatter.buildValueResponse(keyValueStore.get(tokens[1]));
    }

    private static String handleDeleteCommand(String[] tokens, KeyValueStore keyValueStore) {
        if (tokens.length < 2) {
            return ProtocolFormatter.buildErrorResponse("Syntax error for DELETE command");
        }
        keyValueStore.delete(tokens[1]);
        return ProtocolFormatter.buildOkResponse();
    }
}