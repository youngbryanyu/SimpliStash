package com.youngbryanyu.simplistash.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Unit tests for the read only channel initializer.
 */
public class ReadOnlyChannelInitializerTest {
    @Test
    void initChannelConfiguresPipelineCorrectly() {
        /* Setup */
        ClientHandlerFactory mockFactory = mock(ClientHandlerFactory.class);
        ClientHandler mockHandler = mock(ClientHandler.class);
        when(mockFactory.createReadOnlyClientHandler()).thenReturn(mockHandler);
        ReadOnlyChannelInitializer initializer = new ReadOnlyChannelInitializer(mockFactory);
        SocketChannel mockChannel = mock(SocketChannel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(mockChannel.pipeline()).thenReturn(pipeline);

        /* Call method */
        initializer.initChannel(mockChannel);

        /* Test assertions */
        verify(pipeline).addLast(any(StringDecoder.class), any(StringEncoder.class), any(ClientHandler.class));
    }
}
