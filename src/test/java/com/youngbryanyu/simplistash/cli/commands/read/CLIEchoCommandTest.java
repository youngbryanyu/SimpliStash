package com.youngbryanyu.simplistash.cli.commands.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.read.EchoCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the CLI ECHO command.
 */
public class CLIEchoCommandTest {
    /**
     * The CLI ECHO command under test.
     */
    private CLIEchoCommand command;

    /**
     * Setup before each test
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new CLIEchoCommand();
    }

    /**
     * Test getting the name.
     */
    @Test
    public void testGetName() {
        assertEquals(EchoCommand.NAME, command.getName());
    }

    /**
     * Test getting the usage.
     */
    @Test
    public void testGetUsage() {
        assertEquals("echo <value>", command.getUsage());
    }

    /**
     * Test getting the options.
     */
    @Test
    public void testGetOptions() {
        Options options = command.getOptions();
        assertNotNull(options);
        assertEquals(0, options.getOptions().size());
    }

    /**
     * Test the command with valid args.
     */
    @Test
    public void testEncodeCLICommand_WithValidArgs() throws Exception {
        String[] args = {"echo", "Hello"};
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        assertEquals(ProtocolUtil.encode(EchoCommand.NAME, List.of("Hello"), false, Collections.emptyMap()), encodedCommand);
    }

     /**
     * Test the command with invalid args.
     */
    @Test
    public void testEncodeCLICommand_WithInsufficientArgs() throws Exception {
        String[] args = {"echo"};
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNull(encodedCommand);
    }
}
