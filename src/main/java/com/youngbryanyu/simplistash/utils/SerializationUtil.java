package com.youngbryanyu.simplistash.utils;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Util to help serialization to disk
 */
public class SerializationUtil {
    /**
     * Length-prefixed serialization delimiter.
     */
    private static final String DELIM = "\r\n";

    // TODO: add javadoc
    public static String encode(String value) {
        return String.format("%d%s%s", value.length(), DELIM, value);
    }

    public static String decode(BufferedReader reader) throws IOException {
        int length = Integer.parseInt(reader.readLine()); /* Should behave the same as reading until \r\n */
        char[] buffer = new char[length];
        int readChars = reader.read(buffer, 0, length);
        if (readChars != length) {
            throw new IOException("Unexpected end of input.");
        }
        return new String(buffer);
    }
}
