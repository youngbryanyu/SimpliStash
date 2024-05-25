package com.youngbryanyu.simplistash.server.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.server.client.ClientHandlerFactory;
import com.youngbryanyu.simplistash.utils.IOFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class PrimaryServerTest {
    /**
     * The mocked client handler factory.
     */
    @Mock
    private ClientHandlerFactory clientHandlerFactory;
    /**
     * The mocked logger.
     */
    @Mock
    private Logger mockLogger;
    /**
     * The mocked server bootstrap.
     */
    @Mock
    private ServerBootstrap mockServerBootstrap;
    /**
     * The mocked boss group.
     */
    @Mock
    private EventLoopGroup mockBossGroup;
    /**
     * The mocked worker group.
     */
    @Mock
    private EventLoopGroup mockWorkerGroup;
    /**
     * The mocked channel future.
     */
    @Mock
    private ChannelFuture mockChannelFuture;
    /**
     * The mocked server channel.
     */
    @Mock
    private ServerChannel mockServerChannel;
    /**
     * The mocked channel close future.
     */
    @Mock
    private ChannelFuture mockCloseFuture;
    /**
     * The mocked channel initializer
     */
    @Mock
    private PrimaryChannelInitializer mockChannelInitializer;
    /**
     * The mocked key expiration manager.
     */
    @Mock
    private KeyExpirationManager mockKeyExpirationManager;
    /**
     * The mock IO factory.
     */
    @Mock
    private IOFactory mockIoFactory;
    /**
     * The mock socket.
     */
    @Mock
    private Socket mockSocket;
    /**
     * The mock output writer.
     */
    @Mock
    private PrintWriter mockOut;
    /**
     * The read only server under test.
     */
    private PrimaryServer server;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() throws InterruptedException {
        MockitoAnnotations.openMocks(this);

        when(mockServerBootstrap.group(mockBossGroup, mockWorkerGroup)).thenReturn(mockServerBootstrap);
        when(mockServerBootstrap.channel(any())).thenReturn(mockServerBootstrap);
        when(mockServerBootstrap.childHandler(any(ChannelInitializer.class))).thenReturn(mockServerBootstrap);
        when(mockServerBootstrap.bind(anyInt())).thenReturn(mockChannelFuture);
        when(mockChannelFuture.sync()).thenReturn(mockChannelFuture);
        when(mockChannelFuture.channel()).thenReturn(mockServerChannel);
        when(mockServerChannel.closeFuture()).thenReturn(mockCloseFuture);
        when(mockCloseFuture.sync()).thenReturn(mockCloseFuture);
        doNothing().when(mockKeyExpirationManager).startExpirationTask(any());

        server = new PrimaryServer(mockBossGroup, mockWorkerGroup, mockServerBootstrap,
                mockChannelInitializer, mockKeyExpirationManager, mockLogger, mockIoFactory);
    }

    /**
     * Test {@link PrimaryServer#start()}.
     */
    @Test
    public void testServerStart() throws Exception {
        server.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(Server.DEFAULT_PRIMARY_PORT);
        verify(mockLogger).info(anyString());
        verify(mockCloseFuture).sync();
        verify(mockKeyExpirationManager).startExpirationTask(any());
    }

    /**
     * Test {@link PrimaryServer#start()} with a custom primary port.
     */
    @Test
    public void testServerStart_customPrimaryPort() throws Exception {
        System.setProperty("primaryPort", "8000");

        server.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(8000);
        verify(mockLogger).info(anyString());
        verify(mockCloseFuture).sync();
        assertEquals(8000, server.getPort());

        System.clearProperty("primaryPort");
    }

    /**
     * Test {@link PrimaryServer#start()} with an invalid custom primary port.
     */
    @Test
    public void testServerStart_invalidPrimaryPort() throws Exception {
        System.setProperty("primaryPort", "invalidPort");

        server.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(Server.DEFAULT_PRIMARY_PORT);
        verify(mockLogger).info(anyString());
        verify(mockCloseFuture).sync();
        assertEquals(Server.DEFAULT_PRIMARY_PORT, server.getPort());

        System.clearProperty("primaryPort");
    }

    /**
     * Test {@link PrimaryServer#start()} registering the node as a read replica.
     */
    @Test
    public void testServerStart_registerReplica() throws Exception {
        System.setProperty("masterIp", "localhost");
        System.setProperty("masterPort", "8000");

        when(mockIoFactory.createSocket(anyString(), anyInt())).thenReturn(mockSocket);
        when(mockIoFactory.createWriter(any())).thenReturn(mockOut);
        doNothing().when(mockOut).write(anyString());
        doNothing().when(mockOut).flush();

        server.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(Server.DEFAULT_PRIMARY_PORT);
        verify(mockLogger, atLeast(1)).info(anyString());
        verify(mockCloseFuture).sync();
        verify(mockKeyExpirationManager).startExpirationTask(any());

        /* Connection limit should be set to 1 */
        assertEquals(PrimaryServer.REPLICA_PRIMARY_CONNECTION_LIMIT, server.getMaxConnections());

        System.clearProperty("masterIp");
        System.clearProperty("masterPort");
    }

    /**
     * Test {@link PrimaryServer#start()} registering the node as a read replica
     * with an invalid master port.
     */
    @Test
    public void testServerStart_registerReplica_invalidMasterPort() throws Exception {
        System.setProperty("masterIp", "localhost");
        System.setProperty("masterPort", "invalid");

        when(mockIoFactory.createSocket(anyString(), anyInt())).thenReturn(mockSocket);
        when(mockIoFactory.createWriter(any())).thenReturn(mockOut);
        doNothing().when(mockOut).write(anyString());
        doNothing().when(mockOut).flush();

        server.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(Server.DEFAULT_PRIMARY_PORT);
        verify(mockLogger).info(anyString());
        verify(mockCloseFuture).sync();
        verify(mockKeyExpirationManager).startExpirationTask(any());

        /* Connection limit should be set to 1 */
        assertEquals(Server.MAX_CONNECTIONS_PRIMARY, server.getMaxConnections());

        System.clearProperty("masterIp");
        System.clearProperty("masterPort");
    }

    /**
     * Test {@link PrimaryServer#start()} registering the node as a read replica
     * when an IO exception occurs.
     */
    @Test
    public void testServerStart_registerReplica_IOException() throws Exception {
        System.setProperty("masterIp", "localhost");
        System.setProperty("masterPort", "8000");

        when(mockIoFactory.createSocket(anyString(), anyInt())).thenReturn(mockSocket);
        when(mockIoFactory.createWriter(any())).thenReturn(mockOut);
        doNothing().when(mockOut).write(anyString());
        doThrow(new IOException("forced exception")).when(mockOut).flush();

        assertThrows(IOException.class, () -> server.start());

        /* Connection limit should be set to 1 */
        assertEquals(Server.MAX_CONNECTIONS_PRIMARY, server.getMaxConnections());

        System.clearProperty("masterIp");
        System.clearProperty("masterPort");
    }

    /**
     * Test getting the port.
     */
    @Test
    public void testGetPort() {
        assertEquals(Server.DEFAULT_PRIMARY_PORT, server.getPort());
    }

    /**
     * Test {@link PrimaryServer#incrementConnections()}.
     */
    @Test
    public void testIncrementAndDecrementConnections() {
        for (int i = 0; i < Server.MAX_CONNECTIONS_PRIMARY; i++) {
            assertTrue(server.incrementConnections());
        }
        assertFalse(server.incrementConnections());

        /* Decrement so 1 more connection can fit */
        server.decrementConnections();
        server.decrementConnections();
        assertTrue(server.incrementConnections());
    }
}
