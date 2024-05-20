package com.youngbryanyu.simplistash.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the CLI client factory.
 */
public class CLIClientFactoryTest {
    /**
     * The mocked socket.
     */
    @Mock
    private Socket mockSocket;
    /**
     * The mocked input stream.
     */
    @Mock
    private InputStream mockInputStream;
    /**
     * The mocked output stream.
     */
    @Mock
    private OutputStream mockOutputStream;
    /**
     * The CLI client factory under test.
     */
    private CLIClientFactory cliClientFactory;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        cliClientFactory = new CLIClientFactory();
    }

    /**
     * Test creating the socket.
     */
    @Test
    public void testCreateSocket() throws IOException {
        String ip = "localhost";
        int port = 8080;

        try (MockedConstruction<Socket> mockPaymentService = Mockito.mockConstruction(Socket.class, (mock, context) -> {
            /* Do nothing */
        })) {
            Socket socket = cliClientFactory.createSocket(ip, port);
            assertNotNull(socket);
        }
    }

    /**
     * Test creating the buffered reader.
     */
    @Test
    void testCreateBufferedReader() throws IOException {
        when(mockSocket.getInputStream()).thenReturn(mockInputStream);

        BufferedReader bufferedReader = cliClientFactory.createBufferedReader(mockSocket);

        assertNotNull(bufferedReader);
        verify(mockSocket).getInputStream();
    }

    /**
     * Test creating the print writer.
     */
    @Test
    void testCreatePrintWriter() throws IOException {
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        PrintWriter printWriter = cliClientFactory.createPrintWriter(mockSocket);

        assertNotNull(printWriter);
        verify(mockSocket).getOutputStream();
    }
}
