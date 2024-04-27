package com.youngbryanyu.simplistash.protocol;

/**
 * Class containing protocol constants and methods to help with building
 * protocol responses.
 */
public final class ProtocolUtil {
    /**
     * Delimiter in the protocol separating arguments.
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
     * 
     * @param value The value to send to the client.
     * @return The formatted value to send to the client.
     */
    public static String buildValueResponse(String value) {
        return encode(value);
    }

    /**
     * Builds a response containing an error message to send back to the client.
     * 
     * Errors are sent as two string tokens: ERROR <message>
     * 
     * @param errorMsg The error message to send to the client.
     * @return The formatted error message to send to the client.
     */
    public static String buildErrorResponse(String message) {
        return encode(ERROR_PREFIX) + encode(message);
    }

    /**
     * Builds an "ok" response to the client indicating a successful operation.
     * 
     * @return The formatted "ok" response.
     */
    public static String buildOkResponse() {
        return encode(OK_RESPONSE);
    }

    /**
     * Builds an `null` response to the client indicating a value is `null`.
     * 
     * @return The formatted `null` response.
     */
    public static String buildNullResponse() {
        return encode(NULL_RESPONSE);
    }

    /**
     * Builds an "pong" response to the client indicating that their "ping" was
     * received.
     * 
     * @return The formatted "pong" response.
     */
    public static String buildPongResponse() {
        return encode(PONG_RESPONSE);
    }

    /**
     * Encodes an input string into a lenght-prefixed string that follows the
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
}
