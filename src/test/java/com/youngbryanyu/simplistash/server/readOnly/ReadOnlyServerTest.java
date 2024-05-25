package com.youngbryanyu.simplistash.server.readOnly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.server.client.ClientHandlerFactory;
import com.youngbryanyu.simplistash.server.primary.PrimaryServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Unit tests for the read only server.
 */
public class ReadOnlyServerTest {
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
    private ReadOnlyChannelInitializer mockChannelInitializer;
    /**
     * The read only server under test.
     */
    private ReadOnlyServer server;

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

        server = new ReadOnlyServer(mockBossGroup, mockWorkerGroup, mockServerBootstrap, mockChannelInitializer,
                mockLogger);
    }

    /**
     * Test {@link ReadOnlyServer#start()}.
     */
    @Test
    public void testServerStart() throws Exception {
        server.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(Server.DEFAULT_READ_ONLY_PORT);
        verify(mockLogger).info(anyString());
        verify(mockCloseFuture).sync();
    }

    /**
     * Test {@link PrimaryServer#incrementConnections()}.
     */
    @Test
    public void testIncrementAndDecrementConnections() {
        for (int i = 0; i < Server.MAX_CONNECTIONS_READ_ONLY - 1; i++) {
            assertTrue(server.incrementConnections());
        }
        assertFalse(server.incrementConnections());
        
        /* Decrement so 1 more connection can fit */
        server.decrementConnections();
        server.decrementConnections();
        assertTrue(server.incrementConnections());
    }
}
