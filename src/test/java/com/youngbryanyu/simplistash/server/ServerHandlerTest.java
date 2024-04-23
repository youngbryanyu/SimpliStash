package com.youngbryanyu.simplistash.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.server.ServerHandler;

/**
 * Unit tests for the server handler.
 */
class ServerHandlerTest {
    /**
     * The server handler under test.
     */
    private ServerHandler serverHandler;

    /**
     * The mocked channel selector.
     */
    @Mock
    private Selector mockSelector;

    /**
     * The mocked server socket channel.
     */
    @Mock
    private ServerSocketChannel mockServerSocketChannel;

    /**
     * The mocked server socket.
     */
    @Mock
    private ServerSocket mockServerSocket;

    /**
     * The mocked client socket channel.
     */
    @Mock
    private SocketChannel mockSocketChannel;

    /**
     * The mocked channel selection key.
     */
    @Mock
    private SelectionKey mockSelectionKey;

    /**
     * Set up before all tests run.
     */
    @BeforeAll
    public static void beforeAllSetup() throws IOException {
        /* Mock static methods before all */
        Mockito.mockStatic(Selector.class);
        Mockito.mockStatic(ServerSocketChannel.class);
    }

    /**
     * Set up before each test runs.
     */
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        /* Mock static methods in the constructor of ServerHandler */
        when(Selector.open()).thenReturn(mockSelector);
        when(ServerSocketChannel.open()).thenReturn(mockServerSocketChannel);

        /* Mock methods in constructor of ServerHandler */
        when(mockServerSocketChannel.configureBlocking(false)).thenReturn(mockServerSocketChannel);
        when(mockServerSocketChannel.socket()).thenReturn(mockServerSocket);
        doNothing().when(mockServerSocket).bind(any(InetSocketAddress.class));
        when(mockServerSocketChannel.register(mockSelector, SelectionKey.OP_ACCEPT)).thenReturn(null);

        /* Initialize the ServerHandler with a specific port */
        serverHandler = new ServerHandler(8080);
    }

    /**
     * Tests the constructor of the server handler.
     */
    @Test
    void testConstructor() throws IOException {
        verify(mockServerSocketChannel).configureBlocking(false);
        verify(mockServerSocket).bind(new InetSocketAddress(8080));
        verify(mockServerSocketChannel).register(mockSelector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Tests accepting an incoming client connection.
     */
    @Test
    void testHandleAccept() throws IOException {
        when(mockSelector.select()).thenReturn(1).thenReturn(0);
        when(mockSelector.isOpen())
                .thenReturn(true)
                .thenReturn(false); /* Return false on 2nd loop iteration's invocation to stop server */
        when(mockServerSocketChannel.accept()).thenReturn(mockSocketChannel);
        when(mockSelector.selectedKeys()).thenReturn(new HashSet<>(Set.of(mockSelectionKey)));
        when(mockSelectionKey.isAcceptable()).thenReturn(true);

        serverHandler.startServer();

        verify(mockSelector, times(2)).select();
        verify(mockSelector, times(2)).isOpen();
        verify(mockSelector).selectedKeys();
        verify(mockSelectionKey).isAcceptable();
        verify(mockServerSocketChannel).accept();
        verify(mockSocketChannel).configureBlocking(false);
        verify(mockSocketChannel).register(mockSelector, SelectionKey.OP_READ);
        verify(mockSocketChannel).getRemoteAddress();
    }

    /**
     * Tests when an IOException is thrown and caught while accepting an incoming
     * client connection.
     */
    @Test
    void testHandleAccept_catchIOException() throws IOException {
        when(mockSelector.select()).thenReturn(1).thenReturn(0);
        when(mockSelector.isOpen())
                .thenReturn(true)
                .thenReturn(false); /* Return false on 2nd loop iteration's invocation to stop server */
        when(mockServerSocketChannel.accept()).thenThrow(new IOException("Forced IOException"));
        when(mockSelector.selectedKeys()).thenReturn(new HashSet<>(Set.of(mockSelectionKey)));
        when(mockSelectionKey.isAcceptable()).thenReturn(true);

        assertDoesNotThrow(() -> serverHandler.startServer());

        verify(mockSelector, times(2)).select();
        verify(mockSelector, times(2)).isOpen();
        verify(mockSelector).selectedKeys();
        verify(mockSelectionKey).isAcceptable();
        verify(mockServerSocketChannel).accept();
        verify(mockSocketChannel, times(0)).configureBlocking(false);
        verify(mockSocketChannel, times(0)).register(mockSelector, SelectionKey.OP_READ);
        verify(mockSocketChannel, times(0)).getRemoteAddress();
    }

    /**
     * Test reading from and writing to a client.
     */
    @Test
    void testHandleReadAndWrite() throws IOException {
        String input = "Hello";
        ByteBuffer buffer = ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8));
        when(mockSelector.select()).thenReturn(1).thenReturn(1);
        when(mockSelector.isOpen())
                .thenReturn(true)
                .thenReturn(false); /* Return false on 2nd loop iteration's invocation to stop server */
        when(mockServerSocketChannel.accept()).thenReturn(mockSocketChannel);
        when(mockSelector.selectedKeys()).thenReturn(new HashSet<>(Set.of(mockSelectionKey)));
        when(mockSelectionKey.isAcceptable())
                .thenReturn(false); /* Return false so code goes into isReadable block */
        when(mockSelectionKey.isReadable())
                .thenReturn(true);
        when(mockSelectionKey.channel()).thenReturn(mockSocketChannel);
        when(mockSocketChannel.read(any(ByteBuffer.class))).thenReturn(input.length());
        when(mockSocketChannel.write(any(ByteBuffer.class))).thenReturn(buffer.remaining());

        serverHandler.startServer();

        verify(mockSocketChannel).read(any(ByteBuffer.class));
        verify(mockSocketChannel).write(any(ByteBuffer.class));
    }

    /**
     * Test when the client closed the connection (read -1 from the buffer which is
     * end of stream)
     */
    @Test
    void testHandleReadAndWrite_clientClosedConnection() throws IOException {
        when(mockSelector.select()).thenReturn(1).thenReturn(1);
        when(mockSelector.isOpen())
                .thenReturn(true)
                .thenReturn(false); /* Return false on 2nd loop iteration's invocation to stop server */
        when(mockServerSocketChannel.accept()).thenReturn(mockSocketChannel);
        when(mockSelector.selectedKeys()).thenReturn(new HashSet<>(Set.of(mockSelectionKey)));
        when(mockSelectionKey.isAcceptable())
                .thenReturn(false); /* Return false so code goes into isReadable block */
        when(mockSelectionKey.isReadable())
                .thenReturn(true);
        when(mockSelectionKey.channel()).thenReturn(mockSocketChannel);
        when(mockSocketChannel.read(any(ByteBuffer.class))).thenReturn(-1); /* Return -1 to mark end of stream */

        serverHandler.startServer();

        verify(mockSocketChannel).read(any(ByteBuffer.class));
        verify(mockSocketChannel).close();
        verify(mockSelectionKey).cancel();
    }

    /**
     * Test when an IOException is thrown and caught while reading from and writing
     * to a client.
     */
    @Test
    void testHandleReadAndWrite_catchIOException() throws IOException {
        when(mockSelector.select()).thenReturn(1).thenReturn(1);
        when(mockSelector.isOpen())
                .thenReturn(true)
                .thenReturn(false); /* Return false on 2nd loop iteration's invocation to stop server */
        when(mockServerSocketChannel.accept()).thenReturn(mockSocketChannel);
        when(mockSelector.selectedKeys()).thenReturn(new HashSet<>(Set.of(mockSelectionKey)));
        when(mockSelectionKey.isAcceptable())
                .thenReturn(false); /* Return false so code goes into isReadable block */
        when(mockSelectionKey.isReadable())
                .thenReturn(true);
        when(mockSelectionKey.channel()).thenReturn(mockSocketChannel);
        when(mockSocketChannel.read(any(ByteBuffer.class))).thenThrow(new IOException("Forced IOException"));

        serverHandler.startServer();

        verify(mockSocketChannel).read(any(ByteBuffer.class));
        verify(mockSocketChannel, times(0)).write(any(ByteBuffer.class));
        verify(mockSelectionKey).cancel();
        verify(mockSocketChannel).close();
    }

    /**
     * Test when an IOException is thrown and caught while reading from and writing
     * to a client, but another IOException is thrown when attempting to perform
     * cleanup and close the client channel.
     */
    @Test
    void testHandleReadAndWrite_catchIOException_failedToCloseClientChannel() throws IOException {
        when(mockSelector.select()).thenReturn(1).thenReturn(1);
        when(mockSelector.isOpen())
                .thenReturn(true)
                .thenReturn(false); /* Return false on 2nd loop iteration's invocation to stop server */
        when(mockServerSocketChannel.accept()).thenReturn(mockSocketChannel);
        when(mockSelector.selectedKeys()).thenReturn(new HashSet<>(Set.of(mockSelectionKey)));
        when(mockSelectionKey.isAcceptable())
                .thenReturn(false); /* Return false so code goes into isReadable block */
        when(mockSelectionKey.isReadable())
                .thenReturn(true);
        when(mockSelectionKey.channel()).thenReturn(mockSocketChannel);
        when(mockSocketChannel.read(any(ByteBuffer.class))).thenThrow(new IOException("Forced IOException"));
        doThrow(new IOException("Forced IOException")).when(mockSocketChannel).close();

        assertDoesNotThrow(() -> serverHandler.startServer());

        verify(mockSocketChannel).read(any(ByteBuffer.class));
        verify(mockSocketChannel, times(0)).write(any(ByteBuffer.class));
        verify(mockSelectionKey).cancel();
        verify(mockSocketChannel).close();
    }

    /**
     * Test stopping the server.
     */
    @Test
    void testStopServer() throws IOException {
        serverHandler.stopServer();

        verify(mockSelector).close();
        verify(mockServerSocketChannel).close();
    }

    /**
     * Test when an IO exception is thrown and caught while stopping the server.
     */
    @Test
    void testStopServer_IOException() throws IOException {
        doThrow(new IOException("Forced IOException")).when(mockSelector).close();

        assertDoesNotThrow(() -> serverHandler.stopServer());

        verify(mockSelector).close();
    }

    /**
     * Tests when an IOException is thrown and caught during the server's loop when
     * the {@link Selector#select()} is called.
     */
    @Test
    void testStartServer_catchIOException() throws IOException {
        when(mockSelector.select()).thenThrow(new IOException("Forced IOException"));

        assertDoesNotThrow(() -> serverHandler.startServer());

        verify(mockSelector, times(1)).select();
        verify(mockSelector, times(0)).selectedKeys();
        verify(mockSelectionKey, times(0)).isAcceptable();
        verify(mockServerSocketChannel, times(0)).accept();
        verify(mockSocketChannel, times(0)).configureBlocking(false);
        verify(mockSocketChannel, times(0)).register(mockSelector, SelectionKey.OP_READ);
        verify(mockSocketChannel, times(0)).getRemoteAddress();
    }

}
