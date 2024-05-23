package com.youngbryanyu.simplistash.commands.writes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.youngbryanyu.simplistash.commands.write.CreateCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the CREATE command.
 */
public class CreateCommandTest {
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
     * The CREATE command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new CreateCommand(mockStashManager);
    }

    /**
     * Test execution with successful CREATE.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        when(mockStashManager.createStash(anyString(), anyBoolean(), anyLong())).thenReturn(true);
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "0"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, times(1)).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution in read-only mode.
     */
    @Test
    public void testExecute_readOnly() {
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "0"));
        String result = command.execute(tokens, true);
        String expected = ProtocolUtil.buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.READ_ONLY_MODE));
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with stash length too long.
     */
    @Test
    public void testExecute_stashNameTooLong() {
        /* Setup */
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Stash.MAX_NAME_LENGTH + 1; i++) {
            sb.append("a");
        }
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", sb.toString(), "0"));
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.STASH_NAME_TOO_LONG));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with stash length too long.
     */
    @Test
    public void testExecute_stashNameToken() {
        /* Setup */
        when(mockStashManager.containsStash(anyString())).thenReturn(true);
        when(mockStashManager.createStash(anyString(), anyBoolean(), anyLong())).thenReturn(true);
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "0"));
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.STASH_NAME_TAKEN));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with the stash limit reached error.
     */
    @Test
    public void testExecute_stashLimitReached() {
        /* Setup */
        when(mockStashManager.createStash(anyString(), anyBoolean(), anyLong())).thenReturn(false);
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "0"));
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.STASH_LIMIT_REACHED));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, times(1)).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with invalid optional args count.
     */
    @Test
    public void testExecute_invalidOptionalArgsCount() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "-1"));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.INVALID_OPTIONAL_ARGS_COUNT));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with not enough tokens for optional args specified.
     */
    @Test
    public void testExecute_notEnoughOptionalTokens() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "1"));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNull(result);
        assertEquals(3, tokens.size());
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with malformed optional args.
     */
    @Test
    public void testExecute_malformedOptionalArgs() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "1", "OFF-HEAP="));
        String expectedResponse = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.MALFORMED_OPTIONAL_ARGS));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test execution with the optional arg OFF_HEAP.
     */
    @Test
    public void testExecute_optionalArgOFF_HEAP() {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("CREATE", "stash1", "1", "OFF_HEAP=false"));
        String expectedResponse = ProtocolUtil.buildOkResponse();
        when(mockStashManager.createStash(anyString(), anyBoolean(), anyLong())).thenReturn(true);

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, times(1)).createStash(anyString(), anyBoolean(), anyLong());
    }

    /**
     * Test the get name method.
     */
    @Test
    public void testGetName() {
        assertEquals("CREATE", command.getName());
    }
}
