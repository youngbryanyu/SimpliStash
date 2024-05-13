package com.youngbryanyu.simplistash.server.primary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.server.client.ClientHandlerFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * The channel initializer for the primary server.
 */
@Component
public class PrimaryChannelInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * The client handler factory.
     */
    private final ClientHandlerFactory clientHandlerFactory;

    /**
     * The constructor.
     * @param clientHandlerFactory The client handler factory.
     */
    @Autowired
    public PrimaryChannelInitializer(ClientHandlerFactory clientHandlerFactory) {
        this.clientHandlerFactory = clientHandlerFactory;
    }

    /**
     * Initializes the channel.
     */
    @Override
    public void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(
            new StringDecoder(CharsetUtil.UTF_8), 
            new StringEncoder(CharsetUtil.UTF_8), 
            clientHandlerFactory.createPrimaryClientHandler() /* Use primary client handler */
        );
    }
}
