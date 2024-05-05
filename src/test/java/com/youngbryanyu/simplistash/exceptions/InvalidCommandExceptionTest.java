package com.youngbryanyu.simplistash.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the invalid command exception.
 */
public class InvalidCommandExceptionTest {
    /**
     * Tests the constructor with just a message.
     */
    @Test
    public void testException_message() {
        String commandName = "invalidCommand";
        InvalidCommandException exception = new InvalidCommandException(commandName);
        assertEquals(String.format("The command %s is not a valid command.", commandName), exception.getMessage());
    }
}
