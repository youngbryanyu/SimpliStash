package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.cache.InMemoryCache;

import mockit.MockUp;

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
    public void testRunServerScript_NormalExecution() throws IOException {
        when(ServerHandlerFactory.createServerHandler(anyInt(), any(InMemoryCache.class))).thenReturn(mockServerHandler);

        Server.main(null);

        verify(mockServerHandler, times(1)).startServer();
    }

    /**
     * Test when an exception is thrown while running the server startup script
     * through the main method, and it propagates up to the main method.
     */
    @Test
    public void testRunServerScript_ThrowsException() throws IOException {
        new MockUp<System>() {
            @mockit.Mock
            public void exit(int value) {
                throw new RuntimeException(String.valueOf(value));
            }
        };

        try {
            when(ServerHandlerFactory.createServerHandler(anyInt(), any(InMemoryCache.class))).thenThrow(new IOException("Forced IOException"));

            Server.main(null);
        } catch (RuntimeException e) {
            Assertions.assertEquals("1", e.getMessage());
        }
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
