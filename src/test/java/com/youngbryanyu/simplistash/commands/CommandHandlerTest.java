package com.youngbryanyu.simplistash.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
import org.slf4j.Logger;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Unit tests for the command handler.
 */
public class CommandHandlerTest {
    /**
     * The mock command factory.
     */
    @Mock
    private CommandFactory commandFactory;
    /**
     * The mock logger.
     */
    @Mock
    private Logger logger;
    /**
     * The mock command.
     */
    @Mock
    private Command command;
    /**
     * The command handler under test.
     */
    private CommandHandler commandHandler;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        commandHandler = new CommandHandler(commandFactory, logger);
    }

    /**
     * Test {@link CommandHandler#handleCommands(Deque, boolean)} with a valid
     * command.
     */
    @Test
    public void testHandleCommands_valid() throws InvalidCommandException {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("command1"));
        when(commandFactory.getCommand("command1")).thenReturn(command);
        when(command.execute(tokens, false)).thenAnswer(invocation -> {
            Deque<String> args = invocation.getArgument(0);
            args.pollFirst();
            return "Success";
        });

        /* Call method */
        String response = commandHandler.handleCommands(tokens, false);

        /* Check assertions */
        assertNotNull(response);
        assertEquals("Success", response);
        verify(command, times(1)).execute(tokens, false);
    }

    /**
     * Test {@link CommandHandler#handleCommands(Deque, boolean)} with an invalid
     * command.
     */
    @Test
    public void testHandleCommands_invalid() throws InvalidCommandException {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("command1"));
        when(commandFactory.getCommand("command1")).thenReturn(command);
        when(command.execute(tokens, false)).thenAnswer(invocation -> {
            Deque<String> args = invocation.getArgument(0);
            args.pollFirst();
            return null;
        });

        /* Call method */
        String response = commandHandler.handleCommands(tokens, false);

        /* Check assertions */
        assertNull(response);
        verify(command, times(1)).execute(tokens, false);
    }

    /**
     * Test {@link CommandHandler#handleCommands(Deque, boolean)} when the command
     * execute returns null.
     */
    @Test
    public void testHandleCommands_nullResponse() throws InvalidCommandException {
        /* Setup */
        Deque<String> tokens = new LinkedList<>(List.of("command1"));
        when(commandFactory.getCommand("command1")).thenThrow(new InvalidCommandException("Invalid command"));

        /* Call method */
        String response = commandHandler.handleCommands(tokens, false);

        /* Check assertions */
        assertNull(response);
        verify(command, never()).execute(tokens, false);
    }
}
