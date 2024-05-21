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
 * Unit tests for the CLI echo command.
 */
public class CLIEchoCommandTest {
    /**
     * The CLI echo command under test.
     */
    @InjectMocks
    private CLIEchoCommand cliEchoCommand;

    /**
     * Setup before each test
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        cliEchoCommand = new CLIEchoCommand();
    }

    /**
     * Test getting the name.
     */
    @Test
    public void testGetName() {
        assertEquals(EchoCommand.NAME, cliEchoCommand.getName());
    }

    /**
     * Test getting the usage.
     */
    @Test
    public void testGetUsage() {
        assertEquals("echo <value>", cliEchoCommand.getUsage());
    }

    /**
     * Test getting the options.
     */
    @Test
    public void testGetOptions() {
        Options options = cliEchoCommand.getOptions();
        assertNotNull(options);
        assertEquals(0, options.getOptions().size());
    }

    /**
     * Test the command with valid args.
     */
    @Test
    public void testEncodeCLICommand_WithValidArgs() throws Exception {
        String[] args = {"echo", "Hello"};
        CommandLine commandLine = new DefaultParser().parse(cliEchoCommand.getOptions(), args);

        String encodedCommand = cliEchoCommand.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        assertEquals(ProtocolUtil.encode(EchoCommand.NAME, List.of("Hello"), false, Collections.emptyMap()), encodedCommand);
    }

     /**
     * Test the command with invalid args.
     */
    @Test
    public void testEncodeCLICommand_WithInsufficientArgs() throws Exception {
        String[] args = {"echo"};
        CommandLine commandLine = new DefaultParser().parse(cliEchoCommand.getOptions(), args);

        String encodedCommand = cliEchoCommand.encodeCLICommand(commandLine);

        assertNull(encodedCommand);
    }
}
