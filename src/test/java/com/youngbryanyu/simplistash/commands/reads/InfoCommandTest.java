package com.youngbryanyu.simplistash.commands.reads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.youngbryanyu.simplistash.commands.Command.ErrorCause;
import com.youngbryanyu.simplistash.commands.read.InfoCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the INFO command.
 */
public class InfoCommandTest {
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
        command = new InfoCommand(mockStashManager);
    }

    /**
     * Test the get name method.
     */
    @Test
    public void testGetName() {
        assertEquals("INFO", command.getName());
    }

    /**
     * Test execution with successful INFO.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        when(mockStash.getInfo()).thenReturn("test info");
        Deque<String> tokens = new LinkedList<>(List.of("INFO", "0"));
        String expectedResponse = ProtocolUtil.buildValueResponse("test info");

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).getInfo();
    }

    /**
     * Test execution with not enough args/tokens.
     */
    @Test
    public void testExecute_notEnoughArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("INFO"));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(1, tokens.size());
        verify(mockStash, never()).getInfo();
    }

    /**
     * Test execution with an invalid optional args count.
     */
    @Test
    public void testExecute_invalidOptionalArgsCount() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("INFO", "invalid_opt_arg_count"));
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).getInfo();
    }

    /**
     * Test execution with not enough optional args.
     */
    @Test
    public void testExecute_notEnoughOptionalArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("INFO", "1"));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(2, tokens.size());
        verify(mockStash, never()).getInfo();
    }

    /**
     * Test execution with malformed optional args.
     */
    @Test
    public void testExecute_malformedOptionalArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("INFO", "1", "NAME"));
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(ErrorCause.MALFORMED_OPTIONAL_ARGS));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).getInfo();
    }

    /**
     * Test execution with the optional arg NAME specified
     */
    @Test
    public void testExecute_optionalArgNAME() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        when(mockStash.getInfo()).thenReturn("test info");
        Deque<String> tokens = new LinkedList<>(List.of("INFO", "1", "NAME=stash1"));
        String expected = ProtocolUtil.buildValueResponse("test info");

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).getInfo();
    }

    /**
     * Test execution with a nonexistent stash.
     */
    @Test
    public void testExecute_stashDoesntExist() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(null);
        Deque<String> tokens = new LinkedList<>(List.of("INFO", "1", "NAME=stash1"));
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(ErrorCause.STASH_DOESNT_EXIST));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).getInfo();
    }
}
