package com.youngbryanyu.simplistash.protocol;

import java.util.Deque;

import com.youngbryanyu.simplistash.exceptions.BrokenProtocolException;
import com.youngbryanyu.simplistash.exceptions.BufferOverflowException;

/**
 * Class that helps parse tokens from input received by clients through the TCP
 * based protocol.
 */
public class TokenParser {
    /**
     * The max buffer size each client can have. This is to prevent the client from
     * eating up too much memory, and to protect against malicious actors. If the
     * max buffer size is reached for a client, they will be disconnected.
     */
    private static final int MAX_BUFFER_SIZE = 1_000_000;

    /**
     * Parses tokens from the input buffer and stores them in the token deque. The
     * algorithm works in a length-prefixed fashion by finding the delimiter,
     * getting the size of the token to read, then reading that exact many bytes.
     * Everything in the DB is treated as a string.
     * 
     * The format of each input should be like: <num_bytes><delimiter><token>
     * 
     * @param buffer The buffer holding unprocessed data from a client.
     * @param tokens The tokens that have been parsed from the client's buffer.
     * @throws BufferOverflowException If the client's buffer exceeds the maximum
     *                                 allowed size.
     * @throws BrokenProtocolException If the client's input doesn't follow the
     *                                 protocol.
     */
    public static void parseTokens(StringBuilder buffer, Deque<String> tokens)
            throws BufferOverflowException, BrokenProtocolException {
        /* Check if the buffer's size has exceeded the allowable limit */
        if (buffer.length() > MAX_BUFFER_SIZE) {
            throw new BufferOverflowException("Input buffer has overflowed");
        }

        String delim = ProtocolFormatter.getDelim();
        int delimLength = delim.length();
        int delimIdx = -1;

        while ((delimIdx = buffer.indexOf(delim)) != -1) {
            /* Get the size of the token */
            int size;
            try {
                size = Integer.parseInt(buffer.substring(0, delimIdx));
            } catch (NumberFormatException e) {
                throw new BrokenProtocolException("The token size is not a valid integer", e);
            }

            /* Ensure the size of the token is at least 1 */
            if (size < 1) {
                throw new BrokenProtocolException("The token size must be at least 1", null);
            }

            /* Get the start and end indices of the token */
            int startIdx = delimIdx + delimLength;
            int endIdx = startIdx + size;

            /*
             * Break if there aren't enough values in the buffer, there may be more that
             * that haven't been sent from the client yet over the TCP connection stream
             */
            if (endIdx > buffer.length()) {
                break;
            }

            /* Add the token to the deque and remove it from the buffer */
            String token = buffer.substring(startIdx, endIdx);
            tokens.addLast(token);
            buffer.delete(0, endIdx);
        }
    }
}

/*
 * 1. look for \r\n. If found get the value.
 * a.
 * 2. if not found:
 * a. if buffer size greater than max value limit
 */
