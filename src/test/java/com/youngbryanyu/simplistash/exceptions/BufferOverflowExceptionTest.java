package com.youngbryanyu.simplistash.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the buffer overflow exception.
 */
public class BufferOverflowExceptionTest {
    /**
     * Tests the constructor.
     */
    @Test
    public void testException() {
        BufferOverflowException exception = new BufferOverflowException();
        assertEquals("The input buffer has overflowed", exception.getMessage());
    }
}
