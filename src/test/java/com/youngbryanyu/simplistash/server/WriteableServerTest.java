package com.youngbryanyu.simplistash.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WriteableServerTest {
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
    private WriteableChannelInitializer mockChannelInitializer;
    /**
     * The mocked key expiration manager.
     */
    @Mock
    private KeyExpirationManager mockKeyExpirationManager;
    /**
     * The read only server under test.
     */
    private WriteableServer writeableServer;

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

        writeableServer = new WriteableServer(mockBossGroup, mockWorkerGroup, mockServerBootstrap,
                mockChannelInitializer, mockKeyExpirationManager, mockLogger);
    }

    /**
     * Test {@link WriteableServer#start()}.
     */
    @Test
    public void testServerStart() throws Exception {
        writeableServer.start();
        verify(mockServerBootstrap).group(mockBossGroup, mockWorkerGroup);
        verify(mockServerBootstrap).channel(NioServerSocketChannel.class);
        verify(mockServerBootstrap).childHandler(any(ChannelInitializer.class));
        verify(mockServerBootstrap).bind(Server.WRITEABLE_PORT);
        verify(mockLogger).info(anyString());
        verify(mockCloseFuture).sync();
        verify(mockKeyExpirationManager).startExpirationTask(any());
    }
}
