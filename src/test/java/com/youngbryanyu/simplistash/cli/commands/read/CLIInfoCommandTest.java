package com.youngbryanyu.simplistash.cli.commands.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.read.InfoCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the CLI INFO command.
 */
public class CLIInfoCommandTest {
    /**
     * The CLI INFO command under test.
     */
    @InjectMocks
    private CLIInfoCommand cliInfoCommand;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        cliInfoCommand = new CLIInfoCommand();
    }

    /**
     * Test getting the name.
     */
    @Test
    public void testGetName() {
        assertEquals(InfoCommand.NAME, cliInfoCommand.getName());
    }

    /**
     * Test getting the usage.
     */
    @Test
    public void testGetUsage() {
        assertEquals("INFO [-name <name>]", cliInfoCommand.getUsage());
    }

    /**
     * Test getting the options.
     */
    @Test
    public void testGetOptions() {
        Options options = cliInfoCommand.getOptions();
        assertNotNull(options);
        for (InfoCommand.OptionalArg optArg : InfoCommand.OptionalArg.values()) {
            assertTrue(options.hasOption(optArg.name().toLowerCase()));
        }
    }

    /**
     * Test encoding with valid args.
     */
    @Test
    public void testEncodeCLICommand_WithValidArgs() throws Exception {
        String[] args = {"info"};
        CommandLine commandLine = new DefaultParser().parse(cliInfoCommand.getOptions(), args);

        String encodedCommand = cliInfoCommand.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        Map<String, String> optArgMap = new HashMap<>();
        assertEquals(ProtocolUtil.encode(InfoCommand.NAME, Collections.emptyList(), true, optArgMap), encodedCommand);
    }

    /**
     * Test encoding with optional args.
     */
    @Test
    public void testEncodeCLICommand_WithOptionalArgs() throws Exception {
        String[] args = {"info", "--name", "testName"};
        CommandLine commandLine = new DefaultParser().parse(cliInfoCommand.getOptions(), args);

        String encodedCommand = cliInfoCommand.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        Map<String, String> optArgMap = new HashMap<>();
        optArgMap.put("name", "testName");
        assertEquals(ProtocolUtil.encode(InfoCommand.NAME, Collections.emptyList(), true, optArgMap), encodedCommand);
    }

    /**
     * Test encoding with insufficient args.
     */
    @Test
    public void testEncodeCLICommand_WithInsufficientArgs() throws Exception {
        String[] args = {};
        CommandLine commandLine = new DefaultParser().parse(cliInfoCommand.getOptions(), args);

        String encodedCommand = cliInfoCommand.encodeCLICommand(commandLine);

        assertNull(encodedCommand);
    }
}
