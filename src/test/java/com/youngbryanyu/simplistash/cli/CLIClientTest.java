package com.youngbryanyu.simplistash.cli;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the CLI client.
 */
public class CLIClientTest {
    /**
     * The mocked socket.
     */
    @Mock
    private Socket mockSocket;
    /**
     * The mocked buffered reader.
     */
    @Mock
    private BufferedReader mockIn;
    /**
     * The mocked print writer.
     */
    @Mock
    private PrintWriter mockOut;
    /**
     * The mocked CLI client factory.
     */
    @Mock
    private CLIClientFactory mockClientFactory;
    /**
     * The CLI client under test.
     */
    private CLIClient cliClient;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        when(mockClientFactory.createSocket(anyString(), anyInt())).thenReturn(mockSocket);
        when(mockClientFactory.createBufferedReader(mockSocket)).thenReturn(mockIn);
        when(mockClientFactory.createPrintWriter(mockSocket)).thenReturn(mockOut);

        cliClient = new CLIClient(mockClientFactory);
    }

    /**
     * Test connecting to the server.
     */
    @Test
    public void testConnect() throws IOException {
        String ip = "127.0.0.1";
        int port = 8080;

        cliClient.connect(ip, port);

        verify(mockClientFactory).createSocket(ip, port);
        verify(mockClientFactory).createBufferedReader(mockSocket);
        verify(mockClientFactory).createPrintWriter(mockSocket);

        assertNotNull(cliClient.getInputStream());
    }

    /**
     * Test closing the CLI client and its connection.
     */
    @Test
    public void testClose() throws IOException {
        cliClient.connect("127.0.0.1", 8080);

        cliClient.close();

        verify(mockSocket).close();
        verify(mockIn).close();
        verify(mockOut).close();
    }

    /**
     * Test closing the CLI client without ever connecting in the first place.
     */
    @Test
    public void testClose_withoutConnect() throws IOException {
        cliClient.close();

        verify(mockSocket, never()).close();
        verify(mockIn, never()).close();
        verify(mockOut, never()).close();
    }

    /**
     * Test sending a command to the server.
     */
    @Test
    public void testSendCommand() throws IOException {
        cliClient.connect( "127.0.0.1", 8080);
        
        cliClient.sendCommand("testCommand");

        verify(mockOut).print(anyString());
        verify(mockOut).flush();
    }

    /**
     * Test sending a command without connecting to the server first.
     */
    @Test
    public void testSendCommand_withoutConnect() {
        cliClient.sendCommand("testCommand");

        verify(mockOut, never()).print(anyString());
        verify(mockOut, never()).flush();
    }

    /**
     * Test getting the input stream.
     */
    @Test
    public void testGetInputStream() throws IOException {
        cliClient.connect("127.0.0.1", 8080);

        BufferedReader inputStream = cliClient.getInputStream();
        assertNotNull(inputStream);
    }
}
