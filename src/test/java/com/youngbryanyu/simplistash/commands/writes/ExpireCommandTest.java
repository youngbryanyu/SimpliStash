package com.youngbryanyu.simplistash.commands.writes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.youngbryanyu.simplistash.commands.write.DeleteCommand;
import com.youngbryanyu.simplistash.commands.write.ExpireCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the EXPIRE command.
 */
public class ExpireCommandTest {
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
     * The EXPIRE command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new ExpireCommand(mockStashManager);
    }

    /**
     * Test execution with successful EXPIRE.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        when(mockStash.updateTTL(anyString(), anyLong())).thenReturn(true);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "0"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with invalid optional args count.
     */
    @Test
    public void testExecute_invalidOptionalArgsCount() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "-1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with not enough tokens for optional args specified.
     */
    @Test
    public void testExecute_notEnoughOptionalTokens() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "1"));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(4, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution in read-only mode.
     */
    @Test
    public void testExecute_readOnly() {
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "stash1", "1000", "0"));
        String result = command.execute(tokens, true);
        String expected = ProtocolUtil.buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.READ_ONLY_MODE));
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with malformed optional args.
     */
    @Test
    public void testExecute_malformedOptionalArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "1", "NAME="));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.MALFORMED_OPTIONAL_ARGS));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with the optional arg NAME.
     */
    @Test
    public void testExecute_optionalArgNAME() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        when(mockStash.updateTTL(anyString(), anyLong())).thenReturn(true);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "1", "NAME=stash1"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with a stash name that doesn't exist.
     */
    @Test
    public void testExecute_stashDoesntExist() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(null);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "1", "NAME=stash1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.STASH_DOESNT_EXIST));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with an invalid TTL long parameter specified.
     */
    @Test
    public void testExecute_TTLInvalidLong() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "abc", "0"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.TTL_INVALID_LONG));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with a TTL that is out of the supported range (less).
     */
    @Test
    public void testExecute_TTLOutOfRangeLess() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "-1000", "0"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.TTL_OUT_OF_RANGE));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

    /**
     * Test execution with a TTL that is out of the supported range (greater).
     */
    @Test
    public void testExecute_TTLOutOfRangeGreater() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", Long.toString(Long.MAX_VALUE), "0"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.TTL_OUT_OF_RANGE));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(0)).updateTTL(anyString(), anyLong());
    }

     /**
     * Test execution with a key that doesn't exist.
     */
    @Test
    public void testExecute_keyDoesntExist() {
        /* Setup */
        when(mockStashManager.getStash(anyString())).thenReturn(mockStash);
        when(mockStash.updateTTL(anyString(), anyLong())).thenReturn(false);
        Deque<String> tokens = new LinkedList<>(List.of("EXPIRE", "burger", "1000", "0"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.KEY_DOESNT_EXIST));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStash, times(1)).updateTTL(anyString(), anyLong());
    }
}
