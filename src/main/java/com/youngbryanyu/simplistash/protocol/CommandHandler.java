package com.youngbryanyu.simplistash.protocol;

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
     * Parses possible tokens based on the delimiter from the client's
     * current buffer, then applies any full valid command to the in-memory cache.
     * Returns the response to be sent back to the client. Returns null if no
     * command was parsed from the input at all and there is nothing to send back to
     * the client.
     * 
     * The parsing stops once a full command cannot be reached, or once there are no
     * delimiters left.
     * 
     * @param input  The input string to be processed.
     * @param cache  The in-memory cache.
     * @param buffer The buffer of accumulated input from client.
     * @return The response to be sent back to the client, or null if no command was
     *         handled and executed.
     */
    public static String handleCommands(String input, InMemoryCache cache, StringBuilder buffer) {
        String delim = ProtocolFormatter.getDelim();
        int delimLength = delim.length();
        int token1End;
        boolean moreValidTokensLeft = true;

        StringBuilder response = new StringBuilder();

        while (moreValidTokensLeft && (token1End = buffer.indexOf(delim)) != -1) {
            System.out.println("[LOG] Buffer before processing: " + buffer.toString());

            /* Get the first token and get its corresponding command enum */
            String token1 = buffer.substring(0, token1End);
            Command command;
            try {
                command = Command.fromString(token1);
            } catch (InvalidCommandException e) {
                /* Discard chunk if not a valid command */
                buffer.delete(0, token1End + delimLength);
                System.out.println("[LOG] Buffer after discarding: " + buffer.toString());
                continue;
            }

            /*
             * Try to get the 2nd and 3rd args necessary for each command. If the args
             * exist, executes the command on the cache, clears the tokens from the buffer,
             * and returns the response to send to the client. Else, returns null indicating
             * no operation was performed.
             */
            int token2End;
            int token3End;
            String token2;
            String token3;
            switch (command) {
                case SET:
                    token2End = buffer.indexOf(delim, token1End + delimLength);
                    if (token2End == -1) {
                        moreValidTokensLeft = false;
                        break;
                    }
                    token3End = buffer.indexOf(delim, token2End + delimLength);
                    if (token3End == -1) {
                        moreValidTokensLeft = false;
                        break;
                    }
                    token2 = buffer.substring(token1End + delimLength, token2End);
                    token3 = buffer.substring(token2End + delimLength, token3End);
                    buffer.delete(0, token3End + delimLength);
                    response.append(handleSetCommand(token2, token3, cache));
                    break;
                case GET:
                    token2End = buffer.indexOf(delim, token1End + delimLength);
                    if (token2End == -1) {
                        moreValidTokensLeft = false;
                        break;
                    }
                    token2 = buffer.substring(token1End + delimLength, token2End);
                    buffer.delete(0, token2End + delimLength);
                    response.append(handleGetCommand(token2, cache));
                    break;
                case DELETE:
                    token2End = buffer.indexOf(delim, token1End + delimLength);
                    if (token2End == -1) {
                        moreValidTokensLeft = false;
                        break;
                    }
                    token2 = buffer.substring(token1End + delimLength, token2End);
                    buffer.delete(0, token2End + delimLength);
                    response.append(handleDeleteCommand(token2, cache));
                    break;
                default:
                    /* This code shouldn't be reached since invalid commands are discarded */
                    return ProtocolFormatter.buildErrorResponse("Invalid command: " + token1);
            }

            System.out.println("[LOG] Buffer after processing: " + buffer.toString());
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
        return ProtocolFormatter.buildValueResponse(cache.get(key));
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