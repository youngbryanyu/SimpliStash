package com.youngbryanyu.simplistash.commands.reads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.read.PingCommand;

/* Unit tests for the PING command */
public class PingCommandTest {
    /**
     * The PING command under test.
     */
    private PingCommand pingCommand;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        pingCommand = new PingCommand();
    }

    /**
     * Test execution with successful PONG response.
     */
    @Test
    public void testExecute_pong() {
        Deque<String> tokens = new LinkedList<>(List.of("PING"));
        String expectedResponse = "4\r\nPONG";
        String result = pingCommand.execute(tokens, false);
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
        String result = pingCommand.execute(tokens, false);
        assertNull(result);
    }

}
