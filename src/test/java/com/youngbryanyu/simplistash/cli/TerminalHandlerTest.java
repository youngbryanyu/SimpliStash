package com.youngbryanyu.simplistash.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the terminal handler.
 */
public class TerminalHandlerTest {
    /**
     * The mocked line reader
     */
    @Mock
    private LineReader mockLineReader;
    /**
     * The terminal handler under test.
     */
    private TerminalHandler terminalHandler;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        terminalHandler = new TerminalHandler(mockLineReader);
    }

    /**
     * Test reading normal input.
     */
    @Test
    public void testReadLineNormalInput() {
        when(mockLineReader.readLine(anyString())).thenReturn("userInput");

        String result = terminalHandler.readLine("prompt");

        assertEquals("userInput", result);
        verify(mockLineReader).readLine(anyString());
    }

    /**
     * Test an interrupt during reading input from CLI.
     */
    @Test
    public void testReadLineUserInterrupt() {
        when(mockLineReader.readLine(anyString())).thenThrow(new UserInterruptException("interrupt"));

        String result = terminalHandler.readLine("prompt");

        assertEquals("", result);
        verify(mockLineReader).readLine(anyString());
    }

    /**
     * Test an EOF during reading input from CLI.
     */
    @Test
    public void testReadLineEOF() {
        when(mockLineReader.readLine(anyString())).thenThrow(new EndOfFileException("interrupt"));

        String result = terminalHandler.readLine("prompt");

        assertEquals(CLI.EXIT, result);
        verify(mockLineReader).readLine(anyString());
    }

    /**
     * Test reading EOF
     */
    @Test
    public void testReadLineEndOfFile() {
        when(mockLineReader.readLine(anyString())).thenThrow(new EndOfFileException());

        String result = terminalHandler.readLine("prompt");

        assertEquals(CLI.EXIT, result);
        verify(mockLineReader).readLine(anyString());
    }
}
