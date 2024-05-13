package com.youngbryanyu.simplistash.server.readOnly;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.server.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * The server with read-only permissions which listens for incoming client
 * connections. Creates client handlers with read-only permissions.
 */
@Component
public class ReadOnlyServer implements Server {
    /**
     * The application logger.
     */
    private final Logger logger;
    /**
     * The boss event loop group.
     */
    private final EventLoopGroup bossGroup;
    /**
     * The worker event loop group.
     */
    private final EventLoopGroup workerGroup;
    /**
     * The server bootstrap.
     */
    private final ServerBootstrap bootstrap;
    /**
     * The read only channel initializer.
     */
    private final ReadOnlyChannelInitializer channelInitializer;

    /**
     * Constructor for the server.
     * 
     * @param cache The in-memory cache to store data to.
     */
    @Autowired
    public ReadOnlyServer(EventLoopGroup bossGroup,
            EventLoopGroup workerGroup,
            ServerBootstrap bootstrap,
            ReadOnlyChannelInitializer channelInitializer,
            Logger logger) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.bootstrap = bootstrap;
        this.channelInitializer = channelInitializer;
        this.logger = logger;
    }

    /**
     * Starts the server. Creates boss threads to accept incoming connections and
     * worker threads to handle commands from connected clients. Each worker thread
     * runs an event loop that handles I/O in a non-blocking fashion. We use the
     * default number of threads in each group.
     * 
     * @throws Exception If the server fails to start.
     */
    public void start() throws Exception {
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);

            /* Bind to port and listen for connections */
            ChannelFuture f = bootstrap.bind(Server.READ_ONLY_PORT).sync();
            logger.info("Read-only server started on port: " + Server.READ_ONLY_PORT);

            /* Wait until server is closed */
            f.channel().closeFuture().sync();
        } finally {
            /* Cleanup */
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
