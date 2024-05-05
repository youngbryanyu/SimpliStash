package com.youngbryanyu.simplistash.commands;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;

/**
 * Unit tests for the command factory.
 */
public class CommandFactoryTest {
    /**
     * The mocked command 1.
     */
    @Mock
    private Command command1;
    /**
     * Command 1's name.
     */
    private String command1Name = "command1";
    /**
     * The mocked command 2.
     */
    @Mock
    private Command command2;
    /**
     * Command 2's name.
     */
    private String command2Name = "command2";
    /**
     * The command factory under test.
     */
    private CommandFactory commandFactory;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(command1.getName()).thenReturn(command1Name);
        when(command2.getName()).thenReturn(command2Name);

        /* Initialize command factory */
        List<Command> commandList = Arrays.asList(command1, command2);
        commandFactory = new CommandFactory(commandList);
    }

    /**
     * Test {@link CommandFactory#getCommand(String)} with valid command names.
     */
    @Test
    public void testGetCommand_valid() throws InvalidCommandException {
        assertSame(command1, commandFactory.getCommand(command1Name));
        assertSame(command2, commandFactory.getCommand(command2Name));
    }

    /**
     * Test {@link CommandFactory#getCommand(String)} with invalid command names.
     */
    @Test
    public void testGetCommand_invalid() {
        String invalidCommandName = "nonexistentCommand";
        assertThrows(InvalidCommandException.class, () -> {
            commandFactory.getCommand(invalidCommandName);
        });
    }
}
