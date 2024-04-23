package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

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
        ServerHandler handler = ServerHandlerFactory.createServerHandler(port);
        assertNotNull(handler, "ServerHandler should not be null");
    }

    /**
     * Test instantiating the factory.
     */
    @Test
    public void testCreateInstance() {
        ServerHandlerFactory serverHandlerFactory = new ServerHandlerFactory();
        assertNotNull(serverHandlerFactory);
    }
}
