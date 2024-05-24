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

    /**
     * Encodes a single token for serialization.
     * 
     * @param value The value to be serialized.
     * @return The serialized version of the string.
     */
    public static String encode(String value) {
        return String.format("%d%s%s", value.length(), DELIM, value);
    }

    /**
     * Decodes a single token from a file.
     * 
     * @param reader The reader.
     * @return The decoded token.
     * @throws IOException If an IO exception occurs.
     */
    public static String decode(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        if (line == null) {
            return null; /* Nothing left to read */
        }

        /* Parse next item using prefixed strings */
        int length = Integer.parseInt(line);
        char[] buffer = new char[length];
        int readChars = reader.read(buffer, 0, length);
        if (readChars != length) {
            throw new IOException("Unexpected end of input.");
        }
        return new String(buffer);
    }
}
