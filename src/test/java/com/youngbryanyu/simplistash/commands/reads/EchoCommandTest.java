package com.youngbryanyu.simplistash.commands.reads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.commands.read.EchoCommand;

/**
 * Unit tests for the ECHO command.
 */
public class EchoCommandTest {
     /**
     * The ECHO command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new EchoCommand();
    }

    /**
     * Test execution with successful ECHO response.
     */
    @Test
    public void testExecute_echo() {
        Deque<String> tokens = new LinkedList<>(List.of("ECHO", "burger"));
        String expectedResponse = "6\r\nburger";
        String result = command.execute(tokens, false);
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
    }
}
