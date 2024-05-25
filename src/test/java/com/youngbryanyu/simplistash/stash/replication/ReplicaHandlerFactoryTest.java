package com.youngbryanyu.simplistash.stash.replication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import com.youngbryanyu.simplistash.utils.IOFactory;

/**
 * Unit tests for the replica factory.
 */
public class ReplicaHandlerFactoryTest {
    /**
     * The mocked application context.
     */
    @Mock
    private ApplicationContext mockContext;
    /**
     * The mock replica IO factory.
     */
    @Mock
    private IOFactory mockReplicaIOFactory;
    /**
     * The mock replica.
     */
    @Mock
    private ReplicaHandler mockReplica;
    /**
     * The replica factory under test.
     */
    private ReplicaHandlerFactory replicaFactory;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        replicaFactory = new ReplicaHandlerFactory(mockContext);
    }

    /**
     * Test creating the socket.
     */
    @Test
    public void testCreateRreplica() throws IOException {
        when(mockContext.getBean(IOFactory.class)).thenReturn(mockReplicaIOFactory);
        when(mockContext.getBean(eq(ReplicaHandler.class), any(IOFactory.class), anyString(), anyInt())).thenReturn(mockReplica);

        ReplicaHandler result = replicaFactory.createReplica("localhost", 8080);
        assertTrue(result instanceof ReplicaHandler);
        assertEquals(mockReplica, result);
        verify(mockContext).getBean(eq(ReplicaHandler.class), any(IOFactory.class), anyString(), anyInt());
    }
}
