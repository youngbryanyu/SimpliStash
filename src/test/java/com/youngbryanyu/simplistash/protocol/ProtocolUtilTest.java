package com.youngbryanyu.simplistash.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the protocol util.
 */
public class ProtocolUtilTest {
    /**
     * Tests {@link ProtocolUtil#buildValueResponse(String)} when the value is not null.
     */
    @Test
    public void testBuildValueResponse_notNull() {
        String actual = ProtocolUtil.buildValueResponse("value");
        String expected = "5\r\nVALUE5\r\nvalue";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildValueResponse(String)} when the value is null.
     */
    @Test
    public void testBuildValueResponse_null() {
        String actual = ProtocolUtil.buildValueResponse(null);
        String expected = "5\r\nVALUE5\r\n*NULL";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildErrorResponse(String)} with the error message is not null.
     */
    @Test
    public void testBuildErrorResponse_notNull() {
        String actual = ProtocolUtil.buildErrorResponse("message");
        String expected = "5\r\nERROR7\r\nmessage";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildErrorResponse(String)} with the error message is null.
     */
    @Test
    public void testBuildErrorResponse_null() {
        String actual = ProtocolUtil.buildErrorResponse(null);
        String expected = "5\r\nERROR23\r\nUnknown error occurred.";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildOkResponse(String)}.
     */
    @Test
    public void testBuildOkResponse() {
        String actual = ProtocolUtil.buildOkResponse();
        String expected = "5\r\nVALUE2\r\nOK";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildPongResponse(String)}.
     */
    @Test
    public void testBuildPongResponse() {
        String actual = ProtocolUtil.buildPongResponse();
        String expected = "5\r\nVALUE4\r\nPONG";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#encode(String)}.
     */
    @Test
    public void testEncode_string() {
        String actual = ProtocolUtil.encode("token");
        String expected = "5\r\ntoken";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#encode(java.util.List)}.
     */
    @Test
    public void testEncode_list() {
        String actual = ProtocolUtil.encode(List.of("token1", "token2"));
        String expected = "6\r\ntoken16\r\ntoken2";
        assertEquals(expected, actual);
    }
}

// TODO: add test for get min required args as well as new encode
