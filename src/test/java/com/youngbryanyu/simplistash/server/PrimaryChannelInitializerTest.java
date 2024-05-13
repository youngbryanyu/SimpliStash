package com.youngbryanyu.simplistash.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Unit tests for the primary channel initializer.
 */
public class PrimaryChannelInitializerTest {
    /**
     * The mock client handler factory.
     */
    @Mock
    private ClientHandlerFactory mockClientHandlerFactory;
    /**
     * The mock client handler.
     */
    @Mock
    private ClientHandler mockClientHandler;
    /**
     * The mock socket channel.
     */
    @Mock
    private SocketChannel mockSocketChannel;
    /**
     * The mock channel pipeline.
     */
    @Mock
    private ChannelPipeline mockChannelPipeline;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test initializing the socket channel.
     */
    @Test
    public void testInitChannel() {
        /* Setup */
        when(mockClientHandlerFactory.createPrimaryClientHandler()).thenReturn(mockClientHandler);
        PrimaryChannelInitializer initializer = new PrimaryChannelInitializer(mockClientHandlerFactory);
        when(mockSocketChannel.pipeline()).thenReturn(mockChannelPipeline);

        /* Call method */
        initializer.initChannel(mockSocketChannel);

        /* Test assertions */
        verify(mockChannelPipeline).addLast(any(StringDecoder.class), any(StringEncoder.class), any(ClientHandler.class));
    }
}
