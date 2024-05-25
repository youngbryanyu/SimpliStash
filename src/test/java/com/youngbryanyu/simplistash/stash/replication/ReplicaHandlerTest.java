package com.youngbryanyu.simplistash.stash.replication;

import org.checkerframework.checker.units.qual.m;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.utils.IOFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the replica test.
 */
public class ReplicaHandlerTest {
    /**
     * The mock socket.
     */
    @Mock
    private Socket mockSocket;
    /**
     * The mock print writer.
     */
    @Mock
    private PrintWriter mockWriter;
    /**
     * The mock replica factory.
     */
    @Mock
    private IOFactory mockReplicaIOFactory;

    /**
     * The replica class under test.
     */
    private ReplicaHandler replica;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        /* Set up default behavior */
        when(mockReplicaIOFactory.createSocket(anyString(), anyInt())).thenReturn(mockSocket);
        when(mockReplicaIOFactory.createWriter(any(Socket.class))).thenReturn(mockWriter);
        replica = new ReplicaHandler(mockReplicaIOFactory, "127.0.0.1", 8080);
        when(mockSocket.isClosed()).thenReturn(false);
        when(mockSocket.isConnected()).thenReturn(true);
    }

    /**
     * Cleanup after each test.
     */
    @AfterEach
    public void tearDown() {
        replica.close();
    }

    /**
     * Test connecting.
     */
    @Test
    public void testConnect() throws IOException {
        replica.connect();
        verify(mockReplicaIOFactory, times(1)).createSocket("127.0.0.1", 8080);
        verify(mockReplicaIOFactory, times(1)).createWriter(mockSocket);
        assertNotNull(replica.getSocket());
        assertNotNull(replica.getWriter());
    }

    /**
     * Test forwarding a command to a replica.
     */
    @Test
    public void testForwardCommand() throws IOException {
        replica.connect();
        String command = "SET key value\r\n";
        replica.forwardCommand(command);
        verify(mockWriter, times(1)).print(command);
        verify(mockWriter, times(1)).flush();
    }

    /**
     * Test reconnecting.
     */
    @Test
    public void testForwardCommandReconnects() throws IOException {
        replica.connect();
        when(mockSocket.isClosed()).thenReturn(false);
        when(mockSocket.isConnected()).thenReturn(false);
        replica.forwardCommand("SET key value\r\n");
        verify(mockReplicaIOFactory, times(2)).createSocket("127.0.0.1", 8080);
    }

    /**
     * Test forwarding a command with a closed socket.
     */
    @Test
    public void testForwardCommandWithClosedSocket() throws IOException {
        replica.connect();
        when(mockSocket.isClosed()).thenReturn(true);
        replica.forwardCommand("SET key value\r\n");
        verify(mockReplicaIOFactory, times(2)).createSocket("127.0.0.1", 8080);
    }

    /**
     * Test forwarding a command with a null socket.
     */
    @Test
    public void testForwardCommandWithNullSocket() throws IOException {
        when(mockReplicaIOFactory.createSocket(anyString(), anyInt()))
                .thenReturn(null) /* Return null socket first time */
                .thenReturn(mockSocket);
        replica = new ReplicaHandler(mockReplicaIOFactory, "127.0.0.1", 8080);
        replica.connect();
        when(mockSocket.isClosed()).thenReturn(true);
        replica.forwardCommand("SET key value\r\n");
        verify(mockReplicaIOFactory, times(2)).createSocket("127.0.0.1", 8080);
    }

    /**
     * Test forwarding a command with a null socket.
     */
    @Test
    public void testForwardCommandWithNullWriter() throws IOException {
        when(mockReplicaIOFactory.createWriter(any(Socket.class)))
                .thenReturn(null); /* Null writer */
        replica = new ReplicaHandler(mockReplicaIOFactory, "127.0.0.1", 8080);
        replica.connect();
        when(mockSocket.isClosed()).thenReturn(true);
        replica.forwardCommand("SET key value\r\n");
        verify(mockWriter, never()).flush();
        verify(mockWriter, never()).write(anyString());
    }

    /**
     * Test closing the connection.
     * 
     * @throws IOException
     */
    @Test
    public void testClose() throws IOException {
        replica.connect();
        replica.close();
        verify(mockWriter, times(1)).close();
        verify(mockSocket, times(1)).close();
    }

    @Test
    public void testConnectIOException() throws IOException {
        doThrow(new IOException("Test exception")).when(mockReplicaIOFactory).createSocket(anyString(), anyInt());
        replica.connect();
        verify(mockReplicaIOFactory, times(1)).createSocket("127.0.0.1", 8080);
        assertNull(replica.getWriter());
    }

    @Test
    public void testCloseIOException() throws IOException {
        doThrow(new IOException("Test exception")).when(mockSocket).close();
        replica.connect();
        replica.close();
        verify(mockSocket, times(1)).close();
    }
}
