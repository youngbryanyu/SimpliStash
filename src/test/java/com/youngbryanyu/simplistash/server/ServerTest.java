package com.youngbryanyu.simplistash.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for the server interface.
 */
public class ServerTest {
    /**
     * Test {@link Server#start()}.
     */
    @Test
    public void testStartServer() throws Exception {
        Server mockServer = Mockito.mock(Server.class);
        doNothing().when(mockServer).start();
        mockServer.start();
        verify(mockServer).start();
    }

     /**
     * Test {@link Server#start()} when an exception is thrown and caught.
     */
    @Test
    public void testStartServer_handlesException() throws Exception {
        Server mockServer = Mockito.mock(Server.class);

        try {
            doThrow(new Exception("Server failed to start")).when(mockServer).start();
            mockServer.start();
        } catch (Exception e) {
            assertEquals("Server failed to start", e.getMessage());
        }

        verify(mockServer).start();
    }
}
