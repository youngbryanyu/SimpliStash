package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.youngbryanyu.simplistash.server.factory.ServerHandlerFactory;
import com.youngbryanyu.simplistash.server.factory.ServerSocketFactory;
import com.youngbryanyu.simplistash.exceptions.ServerStartupException;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Unit tests for {@link Server}.
 */
public class ServerTest {
    /*
     * Set up before all tests run.
     */
    @BeforeAll
    public static void setup() {
        Mockito.mockStatic(ServerSocketFactory.class);
        Mockito.mockStatic(ServerHandlerFactory.class);
    }

    /**
     * Test running the server startup script through the main method.
     */
    @Test
    public void testRunServerScriptNormalExecution() throws IOException, ServerStartupException {
        ServerSocket mockServerSocket = mock(ServerSocket.class);
        ServerHandler mockServerHandler = mock(ServerHandler.class);

        when(ServerSocketFactory.createServerSocket(Server.PORT)).thenReturn(mockServerSocket);
        when(ServerHandlerFactory.createServerHandler(mockServerSocket)).thenReturn(mockServerHandler);

        Server.main(null);

        verify(mockServerHandler, times(1)).startServer();
    }

    /**
     * Test instantiating server.
     */
    @Test
    public void testInstantiateServerObject() throws IOException, ServerStartupException {
        Server server = new Server();
        assertNotNull(server);
    }
}
