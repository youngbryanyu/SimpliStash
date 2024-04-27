package com.youngbryanyu.simplistash.commands;

import java.util.Deque;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;
import com.youngbryanyu.simplistash.server.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.InMemoryCache;
import com.youngbryanyu.simplistash.stash.Stash;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Class containing methods to help parse the client's input from their buffer
 * into tokens, and handle any commands by applying them to the cache provided.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CommandHandler {
    private final InMemoryCache cache;
    private final Logger logger;

    /**
     * Private constructor to prevent instantiation.
     */
    @Autowired
    public CommandHandler(InMemoryCache cache, Logger logger) {
        this.cache = cache;
        this.logger = logger;
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
     * @throws ValueTooLargeException If the user attempts to set a key or value to
     *                                a value that's over the limit.
     */
    public String handleCommands(Deque<String> tokens) {
        StringBuilder response = new StringBuilder();
        boolean moreFullCommandsLeft = true;

        while (moreFullCommandsLeft && !tokens.isEmpty()) {
            /* Get the command token to execute */
            String token1 = tokens.peekFirst();
            Command command;
            try {
                command = Command.fromString(token1);
            } catch (InvalidCommandException e) {
                tokens.pollFirst(); /* Discard token if not a valid command */
                continue;
            }

            /* Execute the command if there are enough tokens */
            String token2;
            String token3;
            switch (command) {
                case PING:
                    if (tokens.size() < 1) {
                        moreFullCommandsLeft = false;
                        break;
                    }

                    tokens.pollFirst(); /* Remove command token */
                    response.append(handlePingCommand());
                    break;
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
                    return ProtocolUtil.buildErrorResponse("Invalid command: " + token1);
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
    private String handleGetCommand(String key, InMemoryCache cache) {
        String value = cache.get(key);
        return (value == null)
                ? ProtocolUtil.buildNullResponse()
                : ProtocolUtil.buildValueResponse(value);
    }

    /**
     * Handles the SET command.
     * 
     * @param tokens The tokens parsed from the client's input.
     * @param cache  The cache to store values into.
     * @return Returns an OK response.
     * @throws ValueTooLargeException
     */
    private String handleSetCommand(String key, String value, InMemoryCache cache) {
        /* Check if the key or value is too large */
        if (key.length() > Stash.getMaxKeySize()) {
            return ProtocolUtil.buildErrorResponse("The key exceeds the size limit.");
        }

        if (value.length() > Stash.getMaxValueSize()) {
            return ProtocolUtil.buildErrorResponse("The value exceeds the size limit.");
        }

        cache.set(key, value);
        logger.info(String.format("SET %s --> %s\n", key, value));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Handles the DELETE command.
     * 
     * @param tokens The tokens parsed from the client's input.
     * @param cache  The cache to delete values from.
     * @return Returns an OK response.
     */
    private String handleDeleteCommand(String key, InMemoryCache cache) {
        cache.delete(key);
        logger.info(String.format("DELETE %s\n", key));
        return ProtocolUtil.buildOkResponse();
    }

    /**
     * Handles the PING command.
     * 
     * @return The "pong" response.
     */
    private String handlePingCommand() {
        logger.info("PING");
        return ProtocolUtil.buildPongResponse();
    }
}