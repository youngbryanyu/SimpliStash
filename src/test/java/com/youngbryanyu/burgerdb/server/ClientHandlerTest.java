package com.youngbryanyu.burgerdb.server;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@ExtendWith(MockitoExtension.class)
public class ClientHandlerTest {
    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
    }

    @Test
    public void testRun() throws IOException {
        String clientMessage = "Hello from client\n";
        ByteArrayInputStream inStream = new ByteArrayInputStream(clientMessage.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(inStream);
        when(mockSocket.getOutputStream()).thenReturn(outStream);

        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.run();

        verify(mockSocket, atLeastOnce()).getInputStream();
        verify(mockSocket, atLeastOnce()).getOutputStream();
        assert !outStream.toString().isEmpty();
    }

    @Test
    public void testClientDisconnect() throws IOException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(new byte[0]); // empty input stream to simulate end of stream
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(inStream);
        when(mockSocket.getOutputStream()).thenReturn(outStream);

        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.run();

        verify(mockSocket, atLeastOnce()).close();
    }

    @Test
    public void testIOException() throws IOException {
        IOException toBeThrown = new IOException("Forced IOException");
        when(mockSocket.getInputStream()).thenThrow(toBeThrown);

        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.run();

        verify(mockSocket, atLeastOnce()).close();
    }
}
