package com.youngbryanyu.simplistash.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the replica IO factory.
 */
public class IOFactoryTest {
    /**
     * The mocked socket.
     */
    @Mock
    private Socket mockSocket; 
    /**
     * The replica IO factory under test.
     */
    private IOFactory replicaIOFactory;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        replicaIOFactory = new IOFactory();
    }

    /**
     * Test creating the socket.
     */
    @Test
    public void testCreateSocket() throws IOException {
        String ip = "localhost";
        int port = 8080;

        try (MockedConstruction<Socket> mockPaymentService = Mockito.mockConstruction(Socket.class, (mock, context) -> {
            /* Do nothing */
        })) {
            Socket socket = replicaIOFactory.createSocket(ip, port);
            assertNotNull(socket);
        }
    }

     /**
     * Test creating the print writer.
     */
    @Test
    public void testCreateWriter() throws IOException {
        try (MockedConstruction<PrintWriter> mockPaymentService = Mockito.mockConstruction(PrintWriter.class, (mock, context) -> {
            /* Do nothing */
        })) {
            PrintWriter writer = replicaIOFactory.createWriter(mockSocket);
            assertNotNull(writer);
        }
    }
}
