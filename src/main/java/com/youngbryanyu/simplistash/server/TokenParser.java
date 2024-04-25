package com.youngbryanyu.simplistash.server;

import java.util.Deque;

import com.youngbryanyu.simplistash.commands.ProtocolFormatter;

/**
 * Class that helps parse tokens from input received by clients through the TCP
 * based protocol.
 */
public class TokenParser {
    /**
     * Parses tokens from the input buffer which are separated by the delimiter, and
     * stores then in the tokens deque.
     * 
     * @param buffer The buffer holding unprocessed data from a client.
     * @param tokens The tokens that have been parsed from the client's buffer.
     */
    public static void parseTokens(StringBuilder buffer, Deque<String> tokens) {
        String delim = ProtocolFormatter.getDelim();
        int delimLength = delim.length();

        int delimIdx = -1;
        while ((delimIdx = buffer.indexOf(delim)) != -1) {
            String newToken = buffer.substring(0, delimIdx);
            tokens.addLast(newToken);
            buffer.delete(0, delimIdx + delimLength);
        }
    }
}
