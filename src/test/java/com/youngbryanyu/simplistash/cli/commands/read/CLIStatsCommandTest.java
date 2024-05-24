package com.youngbryanyu.simplistash.cli.commands.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.read.StatsCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the CLI STATS command.
 */
public class CLIStatsCommandTest {
    /**
     * The CLI STATS command under test.
     */
    private CLIStatsCommand command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new CLIStatsCommand();
    }

    /**
     * Test getting the name.
     */
    @Test
    public void testGetName() {
        assertEquals(StatsCommand.NAME, command.getName());
    }

    /**
     * Test getting the usage.
     */
    @Test
    public void testGetUsage() {
        assertEquals("STATS", command.getUsage());
    }

    /**
     * Test getting the options.
     */
    @Test
    public void testGetOptions() {
        Options options = command.getOptions();
        assertNotNull(options);
    }

    /**
     * Test encoding with valid args.
     */
    @Test
    public void testEncodeCLICommand_WithValidArgs() throws Exception {
        String[] args = {"stats"};
        CommandLine commandLine = new DefaultParser().parse(command.getOptions(), args);

        String encodedCommand = command.encodeCLICommand(commandLine);

        assertNotNull(encodedCommand);
        assertEquals(ProtocolUtil.encode(StatsCommand.NAME, Collections.emptyList(), false, Collections.emptyMap()), encodedCommand);
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
