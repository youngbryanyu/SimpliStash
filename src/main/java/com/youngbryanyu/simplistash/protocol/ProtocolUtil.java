package com.youngbryanyu.simplistash.protocol;

import java.util.List;

/**
 * Utility class containing protocol constants and methods to help with encoding
 * and building protocol responses.
 */
public final class ProtocolUtil {
    /**
     * Delimiter used in the length-prefixed protocol.
     */
    public static final String DELIM = "\r\n";
    /**
     * The null response.
     */
    private static final String NULL_RESPONSE = "*NULL";
    /**
     * The OK response.
     */
    private static final String OK_RESPONSE = "OK";
    /**
     * The PONG response to a PING command.
     */
    private static final String PONG_RESPONSE = "PONG";
    /**
     * The token that prefixes all error messages
     */
    private static final String ERROR_PREFIX = "ERROR";

    /* Private constructor to prevent instantiation */
    private ProtocolUtil() {
    }

    /**
     * Builds a response containing an arbitrary value to send back to the client.
     * Returns the null response of the value is null.
     * 
     * @param value The value to send to the client.
     * @return The formatted value to send to the client.
     */
    public static String buildValueResponse(String value) {
        if (value == null) {
            return buildNullResponse();
        }
        return encode(value);
    }

    /**
     * Builds a response containing an error message to send back to the client.
     * 
     * Errors are sent in the format: ERROR <message>
     * 
     * @param message The error message to send to the client.
     * @return The encoded error message to send to the client.
     */
    public static String buildErrorResponse(String message) {
        if (message == null) {
            return encode(ERROR_PREFIX) + encode("Unknown error occurred.");
        }
        return encode(ERROR_PREFIX) + encode(message);
    }

    /**
     * Builds an OK response to the client indicating a successful operation.
     * 
     * @return The encoded OK response.
     */
    public static String buildOkResponse() {
        return encode(OK_RESPONSE);
    }

    /**
     * Builds a null response to the client indicating a value is null.
     * 
     * @return The encoded null response.
     */
    public static String buildNullResponse() {
        return encode(NULL_RESPONSE);
    }

    /**
     * Builds an PONG response.
     * 
     * @return The formatted PONG response.
     */
    public static String buildPongResponse() {
        return encode(PONG_RESPONSE);
    }

    /**
     * Encodes an input string into a length-prefixed string that follows the
     * protocol.
     * 
     * The protocol follows the format: <num_bytes><delimiter><token>
     * 
     * @param token The token to send to the client.
     * @return The token after converting it to protocol format.
     */
    public static String encode(String token) {
        return String.format("%s%s%s", token.length(), DELIM, token);
    }

    /**
     * Encodes a list of tokens into a stream of length-prefixed strings that follow
     * the protocol.
     * 
     * @param tokens The list of tokens to encode.
     * @return
     */
    public static String encode(List<String> tokens) {
        StringBuilder result = new StringBuilder();
        for (String token : tokens) {
            result.append(token.length()).append(DELIM).append(token);
        }
        return result.toString();
    }
}
