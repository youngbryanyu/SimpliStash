package com.youngbryanyu.simplistash.commands.reads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
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

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.commands.read.StatsCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the STATS command.
 */
public class StatsCommandTest {
    /**
     * The mock stash manager.
     */
    @Mock
    StashManager mockStashManager;
    /**
     * The mock stash.
     */
    @Mock
    Stash mockStash;
    /**
     * The INFO command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new StatsCommand(mockStashManager);
    }

    /**
     * Test the get name method.
     */
    @Test
    public void testGetName() {
        assertEquals("STATS", command.getName());
    }

    /**
     * Test execution with successful STATS.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        when(mockStashManager.getStats()).thenReturn("stats");
        Deque<String> tokens = new LinkedList<>(List.of("STATS"));
        String expectedResponse = ProtocolUtil.buildValueResponse("stats");

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, times(1)).getStats();
    }

    /**
     * Test execution with not enough args/tokens.
     */
    @Test
    public void testExecute_notEnoughArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of());

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).getStats();
    }
}
