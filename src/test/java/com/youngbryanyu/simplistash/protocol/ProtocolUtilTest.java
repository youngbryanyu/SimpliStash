package com.youngbryanyu.simplistash.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the protocol util.
 */
public class ProtocolUtilTest {
    /**
     * Tests {@link ProtocolUtil#buildValueResponse(String)} when the value is not
     * null.
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
     * Tests {@link ProtocolUtil#buildErrorResponse(String)} with the error message
     * is not null.
     */
    @Test
    public void testBuildErrorResponse_notNull() {
        String actual = ProtocolUtil.buildErrorResponse("message");
        String expected = "5\r\nERROR7\r\nmessage";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildErrorResponse(String)} with the error message
     * is null.
     */
    @Test
    public void testBuildErrorResponse_null() {
        String actual = ProtocolUtil.buildErrorResponse(null);
        String expected = "5\r\nERROR23\r\nUnknown error occurred.";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildFatalResponse(String)} with the error message
     * is not null.
     */
    @Test
    public void testBuildFatalResponse_notNull() {
        String actual = ProtocolUtil.buildFatalResponse("message");
        String expected = "5\r\nFATAL7\r\nmessage";
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#buildFatalResponse(String)} with the error message
     * is null.
     */
    @Test
    public void testBuildFatalResponse_null() {
        String actual = ProtocolUtil.buildFatalResponse(null);
        String expected = "5\r\nFATAL29\r\nUnknown fatal error occurred.";
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

    /**
     * Tests {@link ProtocolUtil#getMinRequiredArgs(String)}.
     */
    @Test
    public void testGetMinRequiredArgs() {
        int actual = ProtocolUtil.getMinRequiredArgs("SET <arg1> [opt1] [opt2]");
        int expected = 2;
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#getMinRequiredArgs(String)} with no optional args.
     */
    @Test
    public void testGetMinRequiredArgs_noOptional() {
        int actual = ProtocolUtil.getMinRequiredArgs("SET <arg1>");
        int expected = 2;
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#encode(String, List, boolean, java.util.Map)}.
     */
    @Test
    public void testEncode_commmand() {
        String actual = ProtocolUtil.encode("SET", List.of("key", "val"), true, Map.of("TTL", "1000"));
        String expected = new StringBuilder()
                .append(ProtocolUtil.encode("SET"))
                .append(ProtocolUtil.encode("key"))
                .append(ProtocolUtil.encode("val"))
                .append(ProtocolUtil.encode("1"))
                .append(ProtocolUtil.encode("TTL=1000"))
                .toString();
        assertEquals(expected, actual);
    }

    /**
     * Tests {@link ProtocolUtil#encode(String, List, boolean, java.util.Map)} for a command with no optional args.
     */
    @Test
    public void testEncode_commmand_noOptional() {
        String actual = ProtocolUtil.encode("ECHO", List.of("hello"), false, Collections.emptyMap());
        String expected = new StringBuilder()
                .append(ProtocolUtil.encode("ECHO"))
                .append(ProtocolUtil.encode("hello"))
                .toString();
        assertEquals(expected, actual);
    }
}
