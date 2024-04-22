package com.youngbryanyu.simplistash.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ServerStartupException}.
 */
public class ServerStartupExceptionTest {
    /**
     * Test that the exception's message is as expected.
     */
    @Test
    public void testExceptionMessage() {
        String expectedMessage = "Server failed to start";
        Throwable cause = new RuntimeException("Underlying issue");

        ServerStartupException exception = new ServerStartupException(expectedMessage, cause);

        assertEquals(expectedMessage, exception.getMessage(), "The message should match the input message");
    }

    /**
     * Test that the exception's cause is as expected.
     */
    @Test
    public void testExceptionCause() {
        String expectedMessage = "Server failed to start";
        Throwable expectedCause = new RuntimeException("Underlying issue");

        ServerStartupException exception = new ServerStartupException(expectedMessage, expectedCause);

        assertEquals(expectedCause, exception.getCause(), "The cause should match the input cause");
    }

    /**
     * Test that the exception's message and cause are as expected when the
     * exception is created and thrown.
     */
    @Test
    public void testThrowException() {
        String expectedMessage = "Server failed to start";
        Throwable expectedCause = new RuntimeException("Underlying issue");

        Exception thrownException = assertThrows(ServerStartupException.class, () -> {
            throw new ServerStartupException(expectedMessage, expectedCause);
        }, "Exception should be thrown correctly");

        assertEquals(expectedMessage, thrownException.getMessage(),
                "Thrown exception should contain the correct message");
        assertEquals(expectedCause, thrownException.getCause(), "Thrown exception should contain the correct cause");
    }
}
