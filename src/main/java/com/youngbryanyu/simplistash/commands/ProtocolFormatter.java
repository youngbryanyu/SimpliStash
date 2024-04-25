package com.youngbryanyu.simplistash.commands;

/**
 * Class containing protocol specifications and methods to help with building
 * protocol responses.
 */
public final class ProtocolFormatter {
    /**
     * Delimiter in the protocol separating arguments.
     */
    private static final String DELIM = "\r\n";
    /**
     * The null response.
     */
    private static final String NULL_RESPONSE = "*NULL";
    /**
     * The OK response. Prefixed with +.
     */
    private static final String OK_RESPONSE = "+OK";
    /**
     * Prefix of all error responses.
     */
    private static final String ERROR_PREFIX = "-";
    /**
     * Prefix of all values send to the client retrieved from the cache.
     */
    private static final String VALUE_PREFIX = "$";

    /* Private constructor to prevent instantiation */
    private ProtocolFormatter() {
    }

    /**
     * Builds a response containing an arbitrary value to send back to the client.
     * 
     * @param value The value to send to the client.
     * @return The formatted value to send to the client.
     */
    public static String buildValueResponse(String value) {
        return String.format("%s%s%s", VALUE_PREFIX, value, DELIM);
    }

    /**
     * Builds a response containing an error message to send back to the client.
     * 
     * @param error The error message to send to the client.
     * @return The formatted error message to send to the client.
     */
    public static String buildErrorResponse(String error) {
        return String.format("%s%s%s", ERROR_PREFIX, error, DELIM);
    }

    /**
     * Builds an "ok" response to the client indicating a successful operation.
     * 
     * @return The formatted "ok" response.
     */
    public static String buildOkResponse() {
        return String.format("%s%s", OK_RESPONSE, DELIM);
    }

    /**
     * Builds an `null` response to the client indicating a value is `null`.
     * 
     * @return The formatted `null` response.
     */
    public static String buildNullResponse() {
        return String.format("%s%s", NULL_RESPONSE, DELIM);
    }

    /**
     * Returns the delimiter used in the protocol to communicate with clients.
     * 
     * @return The protocol delimiter.
     */
    public static String getDelim() {
        return DELIM;
    }
}
