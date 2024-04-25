package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Class containing methods to help parse the client's input from their buffer
 * into tokens, and handle any commands by applying them to the cache provided.
 */
public class CommandHandler {
    /**
     * Private constructor to prevent instantiation.
     */
    private CommandHandler() {
    }

    /**
     * Applies any full valid command from the parsed tokens to the in-memory cache.
     * Returns the response to be sent back to the client. Returns null if no
     * command was parsed from the input at all and there is nothing to send back to
     * the client.
     * 
     * The parsing stops once a full command cannot be reached.
     * 
     * @param cache  The in-memory cache.
     * @param tokens The tokens parsed from the client's input buffer.
     * @return The response to be sent back to the client, or `null` if no command
     *         was handled and executed to indicate to the caller that no response
     *         is needed.
     */
    public static String handleCommands(InMemoryCache cache, Deque<String> tokens) {
        StringBuilder response = new StringBuilder();
        boolean moreFullCommandsLeft = true;

        while (moreFullCommandsLeft && !tokens.isEmpty()) {
            /* Get the first token, but discard it if it's not a valid command */
            String token1 = tokens.peekFirst();
            Command command;
            try {
                command = Command.fromString(token1);
            } catch (InvalidCommandException e) {
                tokens.pollFirst();
                continue;
            }

            /* Execute the command if there are enough tokens for arguments */
            String token2;
            String token3;
            switch (command) {
                case SET:
                    if (tokens.size() < 3) {
                        moreFullCommandsLeft = false;
                        break;
                    }

                    tokens.pollFirst(); /* Remove command token */
                    token2 = tokens.pollFirst();
                    token3 = tokens.pollFirst();
                    response.append(handleSetCommand(token2, token3, cache));
                    break;
                case GET:
                    if (tokens.size() < 2) {
                        moreFullCommandsLeft = false;
                        break;
                    }

                    tokens.pollFirst(); /* Remove command token */
                    token2 = tokens.pollFirst();
                    response.append(handleGetCommand(token2, cache));
                    break;
                case DELETE:
                    if (tokens.size() < 2) {
                        moreFullCommandsLeft = false;
                        break;
                    }
                    tokens.pollFirst(); /* Remove command token */
                    token2 = tokens.pollFirst();
                    response.append(handleDeleteCommand(token2, cache));
                    break;
                default:
                    /* This code shouldn't be reached since invalid commands are discarded */
                    return ProtocolFormatter.buildErrorResponse("Invalid command: " + token1);
            }
        }

        return response.isEmpty() ? null : response.toString();
    }

    /**
     * Handles the GET command.
     * 
     * @param tokens The tokens parsed from the client's input.
     * @param cache  The cache to get values from.
     * @return Returns an OK response.
     */
    private static String handleGetCommand(String key, InMemoryCache cache) {
        String value = cache.get(key);
        return (value == null)
                ? ProtocolFormatter.buildNullResponse()
                : ProtocolFormatter.buildValueResponse(value);
    }

    /**
     * Handles the SET command.
     * 
     * @param tokens The tokens parsed from the client's input.
     * @param cache  The cache to store values into.
     * @return Returns an OK response.
     */
    private static String handleSetCommand(String key, String value, InMemoryCache cache) {
        cache.set(key, value);
        return ProtocolFormatter.buildOkResponse();
    }

    /**
     * Handles the DELETE command.
     * 
     * @param tokens The tokens parsed from the client's input.
     * @param cache  The cache to delete values from.
     * @return Returns an OK response.
     */
    private static String handleDeleteCommand(String key, InMemoryCache cache) {
        cache.delete(key);
        return ProtocolFormatter.buildOkResponse();
    }

    private static String handlePingCommand() {
        // TODO: implement
        return null;
    }
}