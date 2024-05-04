package com.youngbryanyu.simplistash.server;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * The server with write permissions which listens for incoming client
 * connections. Creates client handlers with write and read permissions.
 */
@Component
public class WriteableServer implements Server {
    /**
     * Number of worker threads to use to handle commands. We use a single thread
     * (similar to Redis).
     */
    private static final int NUM_WORKER_THREADS = 1;
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
    public WriteableServer(ClientHandlerFactory clientHandlerFactory, Logger logger) {
        this.clientHandlerFactory = clientHandlerFactory;
        this.logger = logger;
    }

    /**
     * Starts the server. Creates boss threads to accept incoming connections and
     * a single worker thread to handle commands from connected clients. Each worker
     * thread runs an event loop that handles I/O in a non-blocking fashion. Sets up
     * the pipeline of handlers for each client channel.
     * 
     * @throws Exception If the server fails to start.
     */
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(NUM_WORKER_THREADS);

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

                                    /* Create writeable client handler */
                                    clientHandlerFactory.createWriteableClientHandler());
                        }
                    });

            /* Bind to port and listen for connections, then wait until server is closed */
            ChannelFuture f = bootstrap.bind(Server.WRITEABLE_PORT).sync();
            logger.info("Writeable server started on port: " + Server.WRITEABLE_PORT);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
