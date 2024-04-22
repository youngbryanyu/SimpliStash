package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ClientHandler}.
 */
public class ClientHandlerTest {
    /**
     * The mock client socket.
     */
    @Mock
    private Socket mockSocket;

    /**
     * The client handler.
     */
    private ClientHandler clientHandler;

    /*
     * General setup before each unit test.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        clientHandler = new ClientHandler(mockSocket);
    }

    /**
     * Test a successful run of the client socket, where reading from the input
     * stream and writing to the output stream is successful.
     */
    @Test
    public void testRun() throws IOException {
        String clientMessage = "Hello from client\n";
        ByteArrayInputStream inStream = new ByteArrayInputStream(clientMessage.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(inStream);
        when(mockSocket.getOutputStream()).thenReturn(outStream);

        clientHandler.run();

        verify(mockSocket, atLeastOnce()).getInputStream();
        verify(mockSocket, atLeastOnce()).getOutputStream();
        assert !outStream.toString().isEmpty();
    }

    /**
     * Test when the client socket disconnects.
     */
    @Test
    public void testClientDisconnect() throws IOException {
        /* empty input stream to simulate end of stream */
        ByteArrayInputStream inStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(inStream);
        when(mockSocket.getOutputStream()).thenReturn(outStream);

        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.run();

        verify(mockSocket, atLeastOnce()).close();
    }

    /**
     * Test when an IOException is thrown while the client handler is running.
     */
    @Test
    public void testIOException() throws IOException {
        IOException toBeThrown = new IOException("Forced IOException");
        when(mockSocket.getInputStream()).thenThrow(toBeThrown);

        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.run();

        verify(mockSocket, atLeastOnce()).close();
    }

    /**
     * Test when an IOException is thrown when closing the client socket.
     */
    @Test
    public void testCloseServerSocketThrowsIOException() throws IOException {
        /* empty input stream to simulate end of stream */
        ByteArrayInputStream inStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(inStream);
        when(mockSocket.getOutputStream()).thenReturn(outStream);
        doThrow(new IOException("Forced IOException")).when(mockSocket).close();

        ClientHandler clientHandler = new ClientHandler(mockSocket);

        assertDoesNotThrow(() -> clientHandler.run(),
                "No exception should be thrown.");
        verify(mockSocket, atLeastOnce()).close();

    }
}
