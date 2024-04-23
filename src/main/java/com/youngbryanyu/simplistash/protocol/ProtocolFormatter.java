package com.youngbryanyu.simplistash.protocol;

/**
 * Util class used to format responses 
 */
public final class ProtocolFormatter {
    public static final String DELIM = "\r\n";
    public static final String NULL_RESPONSE = "*NULL"; /* Null response */
    public static final String OK_RESPONSE = "+OK"; /* Ok responses are prefixed with + */
    public static final String ERROR_PREFIX = "-"; /* Error responses are prefixed with - */
    public static final String VALUE_PREFIX = "$"; /* Valid values are prefixed with $ */

    private ProtocolFormatter() {
        /* Private constructor to prevent instantiation */
    }

    /**
     * The maximum number of characters that will be read from the client and stored
     * in the local buffer.
     */
    public static final int MAX_INPUT_LENGTH = 1024;

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
}
