package com.youngbryanyu.simplistash.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public static final String PONG_RESPONSE = "PONG";
    /**
     * The token that prefixes all error messages.
     */
    public static final String ERROR_PREFIX = "ERROR";
    /**
     * The token that prefixes all value messages that aren't errors.
     */
    public static final String VALUE_PREFIX = "VALUE";
    /**
     * The token that prefixes fatal errors that should disconnect the client, such
     * as invalid protocol that leads to ambiguous interpretation.
     */
    public static final String FATAL_PREFIX = "FATAL";

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
        return encode(VALUE_PREFIX) + encode(value);
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
     * Builds a response containing an fatal error message to send back to the
     * client.
     * 
     * Errors are sent in the format: FATAL <message>
     * 
     * @param message The error message to send to the client.
     * @return The encoded error message to send to the client.
     */
    public static String buildFatalResponse(String message) {
        if (message == null) {
            return encode(FATAL_PREFIX) + encode("Unknown fatal error occurred.");
        }
        return encode(FATAL_PREFIX) + encode(message);
    }

    /**
     * Builds an OK response to the client indicating a successful operation.
     * 
     * @return The encoded OK response.
     */
    public static String buildOkResponse() {
        return encode(VALUE_PREFIX) + encode(OK_RESPONSE);
    }

    /**
     * Builds a null response to the client indicating a value is null.
     * 
     * @return The encoded null response.
     */
    public static String buildNullResponse() {
        return encode(VALUE_PREFIX) + encode(NULL_RESPONSE);
    }

    /**
     * Builds an PONG response.
     * 
     * @return The formatted PONG response.
     */
    public static String buildPongResponse() {
        return encode(VALUE_PREFIX) + encode(PONG_RESPONSE);
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

    /**
     * Encodes a command, required args, optional args into a format that follows
     * the protocol. Converts all command names and optional arg names to upper case
     * since the server is case-sensitive. Also converts all '-' to '_' since CLI
     * arguments use '-' in place of all '_' in the protocol.
     * 
     * Example 1:
     * - command: set
     * - requiredArgs: {john, cena}
     * - takesOptionalArgs: true
     * - optionalArgs: {ttl=5000, name=stash1}
     * 
     * The output would be in the form (assume each token is encoded with
     * length-prefixing, with no spaces):
     * - SET john cena 2 TTL=5000 NAME=stash1
     * 
     * Example 2:
     * - command: echo
     * - requiredArgs: {hello}
     * - takesOptionalArgs: false
     * - optionalArgs: {}
     * 
     * The output would be in the form (assume each token is encoded with
     * length-prefixing, with no spaces):
     * - ECHO hello
     * 
     * @param command           The command name.
     * @param requiredArgs      The requires arguments.
     * @param takesOptionalArgs Whether or not the command takes optional args. If
     *                          it does, the number of optional args must be
     *                          specified along with the optional args even if 0 are
     *                          used.
     * @param optionalArgs      The optional arguments.
     * @return The encoded command following the protocol to be sent to the server.
     */
    public static String encode(String command, List<String> requiredArgs, boolean takesOptionalArgs,
            Map<String, String> optionalArgs) {
        StringBuilder sb = new StringBuilder();

        /* Append encoded command */
        sb.append(ProtocolUtil.encode(command.toUpperCase())); /* Use upper case command */

        /* Append encoded required args */
        sb.append(ProtocolUtil.encode(requiredArgs));

        /* If the command needs number of optional args specified, add it */
        if (takesOptionalArgs) {
            sb.append(ProtocolUtil.encode(Integer.toString(optionalArgs.size())));

            optionalArgs.forEach((arg, val) -> {
                /* Use upper case arg name and replace "-" with "_" */
                String optionalArg = String.format("%s=%s",
                        arg.toUpperCase().replace("-", "_"),
                        val);

                sb.append(ProtocolUtil.encode(optionalArg));
            });
        }

        return sb.toString();
    }

    /**
     * Gets the minimum number of required arguments to execute a command based on
     * the format. Optional arguments are in [...].
     * 
     * The specification should always be the following for both the protocol and
     * CLI input:
     * - COMMAND <required_args> ... [optional_args] ...
     * 
     * @return The minimum number of required arguments for a command.
     */
    public static int getMinRequiredArgs(String format) {
        /* Remove the optional part, that starts with [ */
        int startOfOptionalIndex = format.indexOf("[");
        String requiredPart = format;
        if (startOfOptionalIndex != -1) {
            requiredPart = format.substring(0, startOfOptionalIndex);
        }

        /* Return the number of required args */
        return (int) Arrays.stream(requiredPart.split(" "))
                .count();
    }
}
