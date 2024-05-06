package com.youngbryanyu.simplistash.commands.writes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import com.youngbryanyu.simplistash.commands.read.GetCommand;
import com.youngbryanyu.simplistash.commands.write.DeleteCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the DELETE command.
 */
public class DeleteCommandTest {
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
     * The DELETE command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new DeleteCommand(mockStashManager);
    }

    /**
     * Test execution with successful DELETE response.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        doNothing().when(mockStash).delete(anyString());
        Deque<String> tokens = new LinkedList<>(List.of("DELETE", "burger", "0"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).delete(anyString());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
        verify(mockStash, times(0)).delete(anyString());
    }

    /**
     * Test execution with invalid optional args count.
     */
    @Test
    public void testExecute_invalidOptionalArgsCount() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("DELETE", "burger", "-1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).delete(anyString());
    }

    /**
     * Test execution with not enough tokens for optional args specified.
     */
    @Test
    public void testExecute_notEnoughOptionalTokens() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("DELETE", "burger", "1"));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(3, tokens.size());
        verify(mockStash, times(0)).delete(anyString());
    }

    /**
     * Test execution in read-only mode.
     */
    @Test
    public void testExecute_readOnly() {
        Deque<String> tokens = new LinkedList<>(List.of("DELETE", "stash1", "0"));
        String result = command.execute(tokens, true);
        String expected = ProtocolUtil.buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.READ_ONLY_MODE));
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).delete(anyString());
    }

    /**
     * Test execution with malformed optional args.
     */
    @Test
    public void testExecute_malformedOptionalArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("DELETE", "burger", "1", "NAME="));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.MALFORMED_OPTIONAL_ARGS));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).delete(anyString());
    }

    /**
     * Test execution with the optional arg NAME.
     */
    @Test
    public void testExecute_optionalArgNAME() { // TODO: resume
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        doNothing().when(mockStash).delete(anyString());
        Deque<String> tokens = new LinkedList<>(List.of("GET", "burger", "1", "NAME=stash1"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).delete(anyString());
    }

    /**
     * Test execution with a stash name that doesn't exist.
     */
    @Test
    public void testExecute_stashDoesntExist() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(null);
        Deque<String> tokens = new LinkedList<>(List.of("DELETE", "burger", "1", "NAME=stash1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.STASH_DOESNT_EXIST));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).delete(anyString());
    }
}
