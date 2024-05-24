package com.youngbryanyu.simplistash.cli.commands.write;

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
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.write.ClearCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the CLI CLEAR command.
 */
public class CLIClearCommandTest {
    /**
     * The CLI CLEAR command under test.
     */
    private CLIClearCommand command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new CLIClearCommand();
    }

    /**
     * Test getting the name.
     */
    @Test
    public void testGetName() {
        assertEquals(ClearCommand.NAME, command.getName());
    }

    /**
     * Test getting the usage.
     */
    @Test
    public void testGetUsage() {
        assertEquals("clear [-name <name>]", command.getUsage());
    }

    /**
     * Test getting the options.
     */
    @Test
    public void testGetOptions() {
        Options options = command.getOptions();
        assertNotNull(options);
        for (ClearCommand.OptionalArg optArg : ClearCommand.OptionalArg.values()) {
            assertTrue(options.hasOption(optArg.name().toLowerCase()));
        }
    }

    /**
     * Test encoding with valid args.
     */
    @Test
    public void testEncodeCLICommand_WithValidArgs() throws Exception {
        String[] args = { "clear"};
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        Map<String, String> optArgMap = new HashMap<>();
        assertEquals(ProtocolUtil.encode(ClearCommand.NAME, Collections.emptyList(), true, optArgMap), encodedCommand);
    }

    /**
     * Test encoding with optional args.
     */
    @Test
    public void testEncodeCLICommand_WithOptionalArgs() throws Exception {
        String[] args = { "clear", "--name", "stash1" };
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        Map<String, String> optArgMap = new HashMap<>();
        optArgMap.put("name", "stash1");
        assertEquals(ProtocolUtil.encode(ClearCommand.NAME, Collections.emptyList(), true, optArgMap), encodedCommand);
    }

    /**
     * Test encoding with insufficient args.
     */
    @Test
    public void testEncodeCLICommand_WithInsufficientArgs() throws Exception {
        String[] args = {};
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNull(encodedCommand);
    }
}
