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

    /**
     * The maximum number of characters that will be read from the client and stored
     * in the local buffer.
     */
    public static final int MAX_INPUT_LENGTH = 1024;

    /* Private constructor to prevent instantiation */
    private ProtocolFormatter() {
    }

    // TODO:  add coments
    public static String buildValueResponse(String value) {
        return String.format("%s%s%s", VALUE_PREFIX, value, DELIM);
    }

    public static String buildErrorResponse(String value) {
        return String.format("%s%s%s", ERROR_PREFIX, value, DELIM);
    }

    public static String buildOkResponse() {
        return String.format("%s%s", OK_RESPONSE, DELIM);
    }

    public static String buildNullResponse() {
        return String.format("%s%s", NULL_RESPONSE, DELIM);
    }

    public static String getDelim() {
        return DELIM;
    }
}
