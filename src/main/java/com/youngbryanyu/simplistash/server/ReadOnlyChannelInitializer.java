package com.youngbryanyu.simplistash.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

@Component
public class ReadOnlyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ClientHandlerFactory clientHandlerFactory;

    @Autowired
    public ReadOnlyChannelInitializer(ClientHandlerFactory clientHandlerFactory) {
        this.clientHandlerFactory = clientHandlerFactory;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(
            new StringDecoder(CharsetUtil.UTF_8),  // Decode client input with UTF8
            new StringEncoder(CharsetUtil.UTF_8),  // Encode server output with UTF8
            clientHandlerFactory.createReadOnlyClientHandler()  // Create read-only client handler
        );
    }
}
