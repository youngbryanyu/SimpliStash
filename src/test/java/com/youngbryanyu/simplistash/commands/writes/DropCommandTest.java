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
import com.youngbryanyu.simplistash.commands.write.DropCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the DROP command.
 */
public class DropCommandTest {
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
     * The DROP command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new DropCommand(mockStashManager);
    }

    /**
     * Test execution with successful DROP.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        doNothing().when(mockStashManager).dropStash(anyString());
        Deque<String> tokens = new LinkedList<>(List.of("DROP", "burger"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, times(1)).dropStash(anyString());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
        verify(mockStashManager, times(0)).dropStash(anyString());
    }

    /**
     * Test execution in read-only mode.
     */
    @Test
    public void testExecute_readOnly() {
        Deque<String> tokens = new LinkedList<>(List.of("DROP", "burger"));
        String result = command.execute(tokens, true);
        String expected = ProtocolUtil.buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.READ_ONLY_MODE));
        assertNotNull(result);
        assertEquals(expected, result);
        verify(mockStashManager, times(0)).dropStash(anyString());
    }

    /**
     * Test execution when dropping the default stash.
     */
    @Test
    public void testExecute_defaultStash() {
        Deque<String> tokens = new LinkedList<>(List.of("DROP", StashManager.DEFAULT_STASH_NAME));
        String result = command.execute(tokens, false);
        String expected = ProtocolUtil
                .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.CANNOT_DROP_DEFAULT_STASH));
        assertNotNull(result);
        assertEquals(expected, result);
        verify(mockStashManager, times(0)).dropStash(anyString());
    }
}
