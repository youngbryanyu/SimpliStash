package com.youngbryanyu.simplistash.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the serialization util.
 */
public class SerializationUtilTest {
    /**
     * Test encoding a valid string.
     */
    @Test
    public void testEncode() {
        String value = "testValue";
        String expected = "9\r\ntestValue";
        String result = SerializationUtil.encode(value);
        assertEquals(expected, result);
    }

    /**
     * Test encoding the empty string.
     */
    @Test
    public void testEncodeEmptyString() {
        String value = "";
        String expected = "0\r\n";
        String result = SerializationUtil.encode(value);
        assertEquals(expected, result);
    }

    /**
     * Test decoding a valid serialized token.
     */
    @Test
    public void testDecode() throws IOException {
        String serialized = "9\r\ntestValue";
        BufferedReader reader = new BufferedReader(new StringReader(serialized));
        String result = SerializationUtil.decode(reader);
        assertEquals("testValue", result);
    }

    /**
     * Test decoding an empty string.
     */
    @Test
    public void testDecodeEmptyString() throws IOException {
        String serialized = "0\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(serialized));
        String result = SerializationUtil.decode(reader);
        assertEquals("", result);
    }

    /**
     * Test decoding incomplete input.
     */
    @Test
    public void testDecodeIncompleteInput() {
        String serialized = "9\r\ntestVal";
        BufferedReader reader = new BufferedReader(new StringReader(serialized));
        IOException exception = assertThrows(IOException.class, () -> {
            SerializationUtil.decode(reader);
        });
        assertEquals("Unexpected end of input.", exception.getMessage());
    }

    /**
     * Test decoding null input.
     */
    @Test
    public void testDecodeNullInput() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(""));
        String result = SerializationUtil.decode(reader);
        assertNull(result);
    }
}
