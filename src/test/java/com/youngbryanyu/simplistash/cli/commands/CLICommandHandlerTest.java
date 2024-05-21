package com.youngbryanyu.simplistash.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.cli.CLI;
import com.youngbryanyu.simplistash.cli.CLIClient;
import com.youngbryanyu.simplistash.exceptions.InvalidCommandException;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * Unit tests for the CLI command handler.
 */
public class CLICommandHandlerTest {
    /**
     * The mocked CLI command factory.
     */
    @Mock
    private CLICommandFactory mockCLICommandFactory;
    /**
     * The mocked CLI client.
     */
    @Mock
    private CLIClient mockCLIClient;
    /**
     * The mocked CLI command.
     */
    @Mock
    private CLICommand mockCLICommand;
    /**
     * The mocked input stream.
     */
    @Mock
    private BufferedReader mockInputStream;
    /**
     * The mock command line parser.
     */
    @Mock
    private CommandLineParser mockParser;
    /**
     * The CLI command handler under test.
     */
    @InjectMocks
    private CLICommandHandler cliCommandHandler;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} with empty input.
     */
    @Test
    public void testProcessCommand_EmptyInput() {
        String response = cliCommandHandler.processCommand("");
        assertNull(response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} with an invalid
     * command.
     */
    @Test
    public void testProcessCommand_invalidCommand() throws IOException, InvalidCommandException {
        /* Setup */
        when(mockCLIClient.getInputStream())
                .thenReturn(new BufferedReader(new StringReader(ProtocolUtil.PONG_RESPONSE)));
        when(mockCLICommandFactory.getCommand("INVALID"))
                .thenThrow(new InvalidCommandException("Invalid command"));

        /* Call method */
        String response = cliCommandHandler.processCommand("INVALID");

        /* Test assertions */
        assertEquals("Unknown command: INVALID", response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} with a valid command.
     */
    @Test
    public void testProcessCommand_validCommand() throws IOException, ParseException, InvalidCommandException {
        /* Setup */
        when(mockCLIClient.getInputStream())
                .thenReturn(new BufferedReader(new StringReader(ProtocolUtil.PONG_RESPONSE)));
        when(mockCLICommandFactory.getCommand("PING")).thenReturn(mockCLICommand);
        when(mockCLICommand.getOptions()).thenReturn(new Options());
        when(mockCLICommand.encodeCLICommand(any(CommandLine.class))).thenReturn("PING");
        when(mockParser.parse(any(), any())).thenReturn(new CommandLine.Builder().build());

        /* Call method */
        String response = cliCommandHandler.processCommand("PING");

        /* Test assertions */
        verify(mockCLIClient, atLeast(1)).sendCommand(anyString());
        assertNotNull(response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} when the server doesn't
     * respond to the PING sent before each command.
     */
    @Test
    public void testProcessCommand_serverDisconnect() throws IOException, ParseException, InvalidCommandException {
        when(mockCLIClient.getInputStream())
                .thenReturn(new BufferedReader(new StringReader("no response")));

        String response = cliCommandHandler.processCommand("PING");
        verify(mockCLIClient, atLeast(1)).sendCommand(anyString());
        assertNotNull(response);
        assertEquals(CLI.EXIT, response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} when the initial PING
     * throws an IO Exception.
     */
    @Test
    public void testProcessCommand_pingIOException() throws IOException, ParseException, InvalidCommandException {
        when(mockCLIClient.getInputStream())
                .thenReturn(mockInputStream);
        when(mockInputStream.read(any(char[].class))).thenThrow(new IOException());

        String response = cliCommandHandler.processCommand("PING");

        verify(mockCLIClient, atLeast(1)).sendCommand(anyString());
        assertNotNull(response);
        assertEquals(CLI.EXIT, response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} when the response from
     * encoding the CLI command to the protocol is null..
     */
    @Test
    public void testProcessCommand_nullCLIEncodeResponse() throws IOException, ParseException, InvalidCommandException {
        /* Setup */
        when(mockCLIClient.getInputStream())
                .thenReturn(new BufferedReader(new StringReader(ProtocolUtil.PONG_RESPONSE)));
        when(mockCLICommandFactory.getCommand("PING")).thenReturn(mockCLICommand);
        when(mockCLICommand.getUsage()).thenReturn("ping");
        when(mockCLICommand.getOptions()).thenReturn(new Options());
        when(mockCLICommand.encodeCLICommand(any(CommandLine.class))).thenReturn(null);

        /* Call method */
        String response = cliCommandHandler.processCommand("PING");

        /* Test assertions */
        verify(mockCLIClient, atLeast(1)).sendCommand(anyString());
        assertNotNull(response);
        assertEquals("Usage: ping", response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} when a parse exception
     * occurs.
     */
    @Test
    public void testProcessCommand_parseException() throws IOException, ParseException, InvalidCommandException {
        /* Setup */
        when(mockCLIClient.getInputStream())
                .thenReturn(new BufferedReader(new StringReader(ProtocolUtil.PONG_RESPONSE)));
        when(mockCLICommandFactory.getCommand("PING")).thenReturn(mockCLICommand);
        when(mockCLICommand.getOptions()).thenReturn(new Options());
        when(mockCLICommand.encodeCLICommand(any(CommandLine.class))).thenReturn("PING");
        when(mockParser.parse(any(), any())).thenThrow(new ParseException("forced exception"));

        /* Call method */
        String response = cliCommandHandler.processCommand("PING");

        /* Test assertions */
        verify(mockCLIClient, atLeast(1)).sendCommand(anyString());
        assertNotNull(response);
        assertEquals("Failed to process command. forced exception", response);
    }

    /**
     * Test {@link CLICommandHandler#processCommand(String)} when the reading the
     * response after sending the command throws an IO Exception.
     */
    @Test
    public void testProcessCommand_IOException() throws IOException, ParseException, InvalidCommandException {
        /* Setup */
        when(mockCLIClient.getInputStream())
                .thenReturn(new BufferedReader(new StringReader(ProtocolUtil.PONG_RESPONSE)))
                .thenReturn(mockInputStream);
        when(mockCLICommandFactory.getCommand("PING")).thenReturn(mockCLICommand);
        when(mockCLICommand.getOptions()).thenReturn(new Options());
        when(mockCLICommand.encodeCLICommand(any(CommandLine.class))).thenReturn("PING");
        when(mockParser.parse(any(), any())).thenReturn(new CommandLine.Builder().build());

        when(mockInputStream.read(any(char[].class))).thenThrow(new IOException("forced exception"));

        /* Call method */
        String response = cliCommandHandler.processCommand("PING");

        /* Test assertions */
        verify(mockCLIClient, atLeast(1)).sendCommand(anyString());
        assertNotNull(response);
        assertEquals(CLI.EXIT, response);
    }

    /**
     * Test {@link CLICommandHandler#readAllFromBuffer()}.
     */
    @Test
    public void testReadAllFromBuffer() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(ProtocolUtil.buildValueResponse("PONG")));
        when(mockCLIClient.getInputStream()).thenReturn(bufferedReader);

        String response = cliCommandHandler.readAllFromBuffer();
        assertEquals("PONG", response);
    }

    /**
     * Test {@link CLICommandHandler#readAllFromBuffer()} with a fatal response.
     */
    @Test
    public void testReadAllFromBuffer_fatal() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(ProtocolUtil.buildFatalResponse("fatal")));
        when(mockCLIClient.getInputStream()).thenReturn(bufferedReader);

        String response = cliCommandHandler.readAllFromBuffer();
        assertEquals(CLI.EXIT, response);
    }

    /**
     * Test {@link CLICommandHandler#readAllFromBuffer()} when there isn't a full
     * response.
     */
    @Test
    public void testReadAllFromBuffer_notFullResponse() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader("OK"));
        when(mockCLIClient.getInputStream()).thenReturn(bufferedReader);

        String response = cliCommandHandler.readAllFromBuffer();
        assertEquals("OK", response);
    }

    /**
     * Test {@link CLICommandHandler#parseTokensFromServer(StringBuilder, Deque)}.
     */
    @Test
    public void testParseTokensFromServer() {
        StringBuilder buffer = new StringBuilder("2\r\nOK");
        Deque<String> tokens = new LinkedList<>();

        cliCommandHandler.parseTokensFromServer(buffer, tokens);

        assertEquals(1, tokens.size());
        assertEquals("OK", tokens.pollFirst());
        assertTrue(buffer.isEmpty());
    }

    /**
     * Test {@link CLICommandHandler#parseTokensFromServer(StringBuilder, Deque)}
     * when theres not enough bytes in the buffer yet based on the length prefix
     * specified..
     */
    @Test
    public void testParseTokensFromServer_notEnoughBytes() {
        StringBuilder buffer = new StringBuilder("3\r\nOK");
        Deque<String> tokens = new LinkedList<>();

        cliCommandHandler.parseTokensFromServer(buffer, tokens);

        assertTrue(tokens.isEmpty());
        assertTrue(!buffer.isEmpty());
        assertEquals("3\r\nOK", buffer.toString());
    }

    /**
     * Test {@link CLICommandHandler#parseArgsFromCLI(String)}.
     */
    @Test
    public void testParseArgsFromCLI() {
        String input = "arg1 \"arg 2\" arg3";
        String[] args = cliCommandHandler.parseArgsFromCLI(input);

        assertEquals(3, args.length);
        assertEquals("arg1", args[0]);
        assertEquals("arg 2", args[1]);
        assertEquals("arg3", args[2]);
    }
}
