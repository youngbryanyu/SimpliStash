package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.exceptions.ServerStartupException;

/**
 * Unit tests for {@link ServerHandler}.
 */
public class ServerHandlerTest {
    /**
     * The Mocked server socket
     */
    @Mock
    private ServerSocket mockServerSocket;

    /**
     * The mocked client socket
     */
    @Mock
    private Socket mockClientSocket;

    /**
     * The server handler under test
     */
    private ServerHandler serverHandler;

    /*
     * General setup before each unit test.
     */
    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        serverHandler = new ServerHandler(mockServerSocket);
    }

    /**
     * Test successfully starting the server
     */
    @Test
    public void testStartServer() throws IOException, InterruptedException {
        Socket mockClientSocket = mock(Socket.class);
        when(mockServerSocket.accept())
                .thenReturn(mockClientSocket)
                .thenThrow(new SocketException("Socket closed")); /* Simulate socket closure on subsequent calls */

        Thread serverThread = new Thread(() -> {
            try {
                serverHandler.startServer();
            } catch (ServerStartupException e) {
                /* Swallow exception */
            }
        });

        serverThread.start();
        Thread.sleep(500); /* Allow time for the server to process the initial accept */

        serverHandler.stopServer(); /* This should lead to "Socket closed" on next accept call */
        serverThread.join(); /* Ensure the server thread has completed before assertions */

        verify(mockServerSocket, times(1)).setReuseAddress(true);
        verify(mockServerSocket, atLeastOnce()).accept();
    }

    /**
     * Test when a socket exception is thrown upon starting the server.
     */
    @Test
    public void testServerStartupException() throws IOException {
        doThrow(new SocketException("Forced error")).when(mockServerSocket).setReuseAddress(true);
        assertThrows(ServerStartupException.class, () -> serverHandler.startServer());
    }

    /**
     * Test successfully stopping the server.
     */
    @Test
    public void testStopServer() throws IOException {
        serverHandler.stopServer();
        verify(mockServerSocket, times(1)).close();
    }

    /**
     * Test that the server is closed when {@link ServerHandler#stopServer} is called.
     */
    @Test
    public void testServerClosesOnStop() throws IOException {
        when(mockServerSocket.isClosed()).thenReturn(false);
        serverHandler.stopServer();
        verify(mockServerSocket, times(1)).close();
    }

    /**
     * Test when an IOException is thrown when stopping the server.
     */
    @Test
    public void testCloseServerSocketThrowsIOException() throws IOException {
        doThrow(new IOException("Forced IOException")).when(mockServerSocket).close();

        ServerHandler serverHandler = new ServerHandler(mockServerSocket);

        assertDoesNotThrow(() -> serverHandler.stopServer(),
                "stopServer should not throw, even when IOException occurs");
        verify(mockServerSocket, times(1)).close();
    }
}
