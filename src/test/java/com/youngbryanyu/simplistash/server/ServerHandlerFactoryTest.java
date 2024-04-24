package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.security.Key;

import org.junit.jupiter.api.Test;

import com.youngbryanyu.simplistash.cache.InMemoryCache;

/**
 * Unit tests for the server handler factory.
 */
public class ServerHandlerFactoryTest {
    /**
     * Test successfully creating a server handler.
     */
    @Test
    public void testCreateServerHandlerSuccess() throws IOException {
        int port = 12345;
        InMemoryCache mockCache = mock(InMemoryCache.class);
        ServerHandler serverHandler = ServerHandlerFactory.createServerHandler(port, mockCache);
        
        assertTrue(serverHandler instanceof ServerHandler);
    }
}
