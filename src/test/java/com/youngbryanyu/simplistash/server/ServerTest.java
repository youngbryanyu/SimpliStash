package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

/**
 * Unit tests for {@link Server}.
 */
public class ServerTest {
    /**
     * The mocked server handler.
     */
    @Mock
    ServerHandler mockServerHandler;

    /**
     * The mocked server handler factory.
     */
    @Mock
    ServerHandlerFactory mockServerHandlerFactory;
    
    /*
     * Set up before all tests run.
     */
    @BeforeAll
    public static void beforeAllSetup() {
        Mockito.mockStatic(ServerHandlerFactory.class); /* Need to use static mock */
    }

    /**
     * Setup before each test runs.
     */
    @BeforeEach
    public void beforeEachSetup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test running the server startup script through the main method.
     */
    @Test
    public void testRunServerScriptNormalExecution() throws IOException {
        when(ServerHandlerFactory.createServerHandler(anyInt())).thenReturn(mockServerHandler);

        Server.main(null);

        verify(mockServerHandler, times(1)).startServer();
    }

    /**
     * Test instantiating server.
     */
    @Test
    public void testInstantiateServerObject() throws IOException {
        Server server = new Server();
        assertNotNull(server);
    }
}
