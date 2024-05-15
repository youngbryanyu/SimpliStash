package com.youngbryanyu.simplistash.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the command interface.
 */
public class CommandTest {
    /**
     * The concrete command implementing the command interface under test.
     */
    class TestCommand implements Command {
        public static final String NAME = "name";
        public static final String EXECUTE_RESPONSE = "executed";

        public String getName() {
            return NAME;
        }
        
        public String execute(Deque<String> tokens, boolean readOnly) {
            return EXECUTE_RESPONSE;
        }
    }

    /**
     * The command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        command = new TestCommand();
    }

    /**
     * Test {@link Command#getNumOptionalArgs(String)} with a valid token.
     */
    @Test
    public void testGetNumOptionalArgs_valid() {
        String token = "5";
        int expected = 5;
        int actual = command.getNumOptionalArgs(token);
        assertEquals(expected, actual);
    }

    /**
     * Test {@link Command#getNumOptionalArgs(String)} when the input is not a valid
     * number string.
     */
    @Test
    public void testGetNumOptionalArgs_invalid() {
        String token = "abc";
        int expected = -1;
        int actual = command.getNumOptionalArgs(token);
        assertEquals(expected, actual);
    }

    /**
     * Test {@link Command#processOptionalArgs(Deque, int)} with valid arguments.
     */
    @Test
    public void testProcessOptionalArgs_valid() {
        Deque<String> tokens = new LinkedList<>(Arrays.asList("key1=value1", "key2=value2"));
        Map<String, String> result = command.processOptionalArgs(tokens, 2);
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    /**
     * Test {@link Command#processOptionalArgs(Deque, int)} with invalid arguments.
     */
    @Test
    public void testProcessOptionalArgs_invalid() {
        Deque<String> tokens = new LinkedList<>(Arrays.asList("key1=value1", "key2="));
        Map<String, String> result = command.processOptionalArgs(tokens, 2);
        assertNull(result);
    }

    /**
     * Test {@link Command#buildErrorMessage(Command.ErrorCause)}.
     */
    @Test
    public void testBuildErrorMessage() {
        String expected = TestCommand.NAME + " failed: The key is too long";
        String actual = command.buildErrorMessage(Command.ErrorCause.KEY_TOO_LONG);
        assertEquals(expected, actual);
    }
}
