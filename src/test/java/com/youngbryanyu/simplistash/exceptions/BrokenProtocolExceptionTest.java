package com.youngbryanyu.simplistash.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the broken protocol exception.
 */
public class BrokenProtocolExceptionTest {
    /**
     * Tests the constructor with just a message.
     */
    @Test
    public void testException_message() {
        String message = BrokenProtocolException.TOKEN_SIZE_INVALID_INTEGER;
        BrokenProtocolException exception = new BrokenProtocolException(message);
        assertEquals(message, exception.getMessage());
    }

    /**
     * Tests the constructor with both message and cause.
     */
    @Test
    public void testException_messageAndCause() {
        String message = BrokenProtocolException.TOKEN_SIZE_OUT_OF_RANGE;
        Throwable cause = new RuntimeException("Cause");

        BrokenProtocolException exception = new BrokenProtocolException(message, cause);

        assertEquals(String.format("Protocol error: %s, disconnecting...", message), exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
