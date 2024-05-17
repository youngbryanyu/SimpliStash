package com.youngbryanyu.simplistash.commands.writes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import com.youngbryanyu.simplistash.commands.write.SetCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the SET command.
 */
public class SetCommandTest {
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
     * The SET command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new SetCommand(mockStashManager);
    }

    /**
     * Test execution with successful SET.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        doNothing().when(mockStash).set(anyString(), anyString());
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "double", "0"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).set(anyString(), anyString());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with invalid optional args count.
     */
    @Test
    public void testExecute_invalidOptionalArgsCount() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "cheese", "-1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with not enough tokens for optional args specified.
     */
    @Test
    public void testExecute_notEnoughOptionalTokens() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "buns", "1"));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(4, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution in read-only mode.
     */
    @Test
    public void testExecute_readOnly() {
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "lettuce", "0"));
        String result = command.execute(tokens, true);
        String expected = ProtocolUtil.buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.READ_ONLY_MODE));
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with malformed optional args.
     */
    @Test
    public void testExecute_malformedOptionalArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "tomato", "1", "NAME="));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.MALFORMED_OPTIONAL_ARGS));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with the optional arg NAME.
     */
    @Test
    public void testExecute_optionalArgNAME() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "ketchup", "1", "NAME=stash1"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).set(anyString(), anyString());
    }

    /**
     * Test execution with a stash name that doesn't exist.
     */
    @Test
    public void testExecute_stashDoesntExist() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(null);
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "mustard", "1", "NAME=stash1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.STASH_DOESNT_EXIST));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with a key that's too long.
     */
    @Test
    public void testExecute_keyTooLong() {
        /* Setup */
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Stash.MAX_KEY_LENGTH + 1; i++) {
            sb.append("a");
        }
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("SET", sb.toString(), "double", "0"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.KEY_TOO_LONG));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with a value that's too long.
     */
    @Test
    public void testExecute_valueTooLong() {
        /* Setup */
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Stash.MAX_VALUE_LENGTH + 1; i++) {
            sb.append("a");
        }
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", sb.toString(), "0"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.VALUE_TOO_LONG));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with invalid TTL long.
     */
    @Test
    public void testExecute_TTLInvalidLong() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "double", "1", "TTL=abc"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.TTL_INVALID_LONG));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with out of range TTL (less).
     */
    @Test
    public void testExecute_TTLOutOfRangeLess() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("SET", "burger", "double", "1", "TTL=-1000"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.TTL_OUT_OF_RANGE));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with out of range TTL (greater).
     */
    @Test
    public void testExecute_TTLOutOfRangeGreater() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(
                List.of("SET", "burger", "double", "1", String.format("TTL=%d", Long.MAX_VALUE)));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.TTL_OUT_OF_RANGE));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, never()).set(anyString(), anyString());
    }

    /**
     * Test execution with a valid TTL.
     */
    @Test
    public void testExecute_TTL_success() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        doNothing().when(mockStash).setWithTTL(anyString(), anyString(), anyLong());
        Deque<String> tokens = new LinkedList<>(
                List.of("SET", "burger", "double", "1", "TTL=5000"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).setWithTTL(anyString(), anyString(), anyLong());
    }

    /**
     * Test the get name method.
     */
    @Test
    public void testGetName() {
        assertEquals("SET", command.getName());
    }
}
