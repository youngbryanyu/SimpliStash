package com.youngbryanyu.simplistash.server;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MaxBytesRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * The server with read-only permissions which listens for incoming client
 * connections. Creates client handlers with read-only permissions.
 */
@Component
public class ReadOnlyServer implements Server {
    /**
     * The client handler factory.
     */
    private final ClientHandlerFactory clientHandlerFactory;
    /**
     * The application logger.
     */
    private final Logger logger;

    /**
     * Constructor for the server.
     * 
     * @param cache The in-memory cache to store data to.
     */
    @Autowired
    public ReadOnlyServer(ClientHandlerFactory clientHandlerFactory, Logger logger) {
        this.clientHandlerFactory = clientHandlerFactory;
        this.logger = logger;
    }

    /**
     * Starts the server. Creates boss threads to accept incoming connections and
     * worker threads to handle commands from connected clients. Each worker thread
     * runs an event loop that handles I/O in a non-blocking fashion. Sets up the
     * pipeline of handlers for each client channel.
     * 
     * @throws Exception If the server fails to start.
     */
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(); /* Use default number of worker threads */

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new StringDecoder(CharsetUtil.UTF_8), /* Decode client input with UTF8 */
                                    new StringEncoder(CharsetUtil.UTF_8), /* Encode server output with UTF8 */

                                    /* Create read-only client handler */
                                    clientHandlerFactory.createReadOnlyClientHandler()); 
                        }
                    });

            /* Bind to port and listen for connections, then wait until server is closed */
            ChannelFuture f = bootstrap.bind(Server.READ_ONLY_PORT).sync();
            logger.info("Read-only server started on port: " + Server.READ_ONLY_PORT);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
