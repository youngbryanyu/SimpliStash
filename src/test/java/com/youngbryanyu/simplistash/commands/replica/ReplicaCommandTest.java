package com.youngbryanyu.simplistash.commands.replica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the REPLICA command.
 */
public class ReplicaCommandTest {
    /**
     * The mock stash manager.
     */
    @Mock
    StashManager mockStashManager;
    /**
     * The REPLICA command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new ReplicaCommand(mockStashManager);
    }

    /**
     * Test execution with successful REPLICA.
     */
    @Test
    public void testExecute_success() {
        /* Setup */
        doNothing().when(mockStashManager).registerReadReplica(anyString(), anyInt());
        Deque<String> tokens = new LinkedList<>(List.of("REPLICA", "localhost", "3000"));
        String expectedResponse = ProtocolUtil.buildOkResponse();

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, times(1)).registerReadReplica(anyString(), anyInt());
    }

    /**
     * Test execution with an invalid port.
     */
    @Test
    public void testExecute_invalidPort() {
        /* Setup */
        doNothing().when(mockStashManager).registerReadReplica(anyString(), anyInt());
        Deque<String> tokens = new LinkedList<>(List.of("REPLICA", "localhost", "invalid3000"));
         String expectedResponse = ProtocolUtil
        .buildErrorResponse(command.buildErrorMessage(Command.ErrorCause.INVALID_PORT));

        /* Call method */
        String result = command.execute(tokens, false);

        /* Perform assertions */
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(0, tokens.size());
        verify(mockStashManager, never()).registerReadReplica(anyString(), anyInt());
    }

    /**
     * Test execution with not enough tokens.
     */
    @Test
    public void testExecute_notEnoughTokens() {
        Deque<String> tokens = new LinkedList<>();
        String result = command.execute(tokens, false);
        assertNull(result);
        verify(mockStashManager, never()).registerReadReplica(anyString(), anyInt());
    }

  
    /**
     * Test the get name method.
     */
    @Test
    public void testGetName() {
        assertEquals("REPLICA", command.getName());
    }
}
