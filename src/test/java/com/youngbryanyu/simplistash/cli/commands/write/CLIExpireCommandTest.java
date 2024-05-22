package com.youngbryanyu.simplistash.cli.commands.write;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.write.DeleteCommand;
import com.youngbryanyu.simplistash.commands.write.ExpireCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the CLI EXPIRE command.
 */
public class CLIExpireCommandTest {
    /**
     * The CLI EXPIRE command under test.
     */
    private CLIExpireCommand command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new CLIExpireCommand();
    }

    /**
     * Test getting the name.
     */
    @Test
    public void testGetName() {
        assertEquals(ExpireCommand.NAME, command.getName());
    }

    /**
     * Test getting the usage.
     */
    @Test
    public void testGetUsage() {
        assertEquals("expire <key> <ttl> [-name <name>]", command.getUsage());
    }

    /**
     * Test getting the options.
     */
    @Test
    public void testGetOptions() {
        Options options = command.getOptions();
        assertNotNull(options);
        for (ExpireCommand.OptionalArg optArg : ExpireCommand.OptionalArg.values()) {
            assertTrue(options.hasOption(optArg.name().toLowerCase()));
        }
    }

    /**
     * Test encoding with valid args.
     */
    @Test
    public void testEncodeCLICommand_WithValidArgs() throws Exception {
        String[] args = { "expire", "key", "5000" };
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        Map<String, String> optArgMap = new HashMap<>();
        assertEquals(ProtocolUtil.encode(ExpireCommand.NAME, List.of("key", "5000"), true, optArgMap), encodedCommand);
    }

    /**
     * Test encoding with optional args.
     */
    @Test
    public void testEncodeCLICommand_WithOptionalArgs() throws Exception {
        String[] args = { "expire", "key", "5000", "--name", "stash1" };
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        Map<String, String> optArgMap = new HashMap<>();
        optArgMap.put("name", "stash1");
        assertEquals(ProtocolUtil.encode(ExpireCommand.NAME, List.of("key", "5000"), true, optArgMap), encodedCommand);
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
