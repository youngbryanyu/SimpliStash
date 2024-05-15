package com.youngbryanyu.simplistash.server.primary;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * The server with write permissions which listens for incoming client
 * connections. Creates client handlers with write and read permissions.
 */
@Component
public class PrimaryServer implements Server {
    /**
     * Number of worker threads to use to handle commands. We use a single thread
     * (similar to Redis).
     */
    public static final int NUM_WORKER_THREADS = 1;
    /**
     * The TTL Expiration manager.
     */
    private final KeyExpirationManager keyExpirationManager;
    /**
     * The boss event loop group.
     */
    private final EventLoopGroup bossGroup;
    /**
     * The worker event loop group with a single thread and thread affinity.
     */
    private final EventLoopGroup workerGroup;
    /**
     * The server bootstrap.
     */
    private final ServerBootstrap bootstrap;
    /**
     * The read only channel initializer.
     */
    private final PrimaryChannelInitializer channelInitializer;
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
    public PrimaryServer(EventLoopGroup bossGroup,
            // @Qualifier(AppConfig.SINGLE_THREADED_NIO_EVENT_LOOP_GROUP) EventLoopGroup workerGroup,
            EventLoopGroup workerGroup,
            ServerBootstrap bootstrap,
            PrimaryChannelInitializer channelInitializer,
            KeyExpirationManager keyExpirationManager,
            Logger logger) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.bootstrap = bootstrap;
        this.channelInitializer = channelInitializer;
        this.keyExpirationManager = keyExpirationManager;
        this.logger = logger;
    }

    /**
     * Starts the server. Creates boss threads to accept incoming connections and
     * a single worker thread to handle commands from connected clients. The worker
     * thread runs an event loop that handles I/O in a non-blocking fashion. Sets up
     * the pipeline of handlers for each client channel.
     * 
     * We bind the worker thread to a single CPU since the writable server's thread
     * is high-priority and manages all the write operation throughput.
     * 
     * @throws Exception If the server fails to start.
     */
    public void start() throws Exception {
        /* Set up periodic task to expire TTLed keys */
        keyExpirationManager.startExpirationTask(workerGroup);

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);

            /* Bind to port and listen for connections */
            ChannelFuture f = bootstrap.bind(Server.PRIMARY_PORT).sync();
            logger.info("Primary server started on port: " + Server.PRIMARY_PORT);

            /* Wait until server is closed */
            f.channel().closeFuture().sync();
        } finally {
            /* Cleanup */
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            keyExpirationManager.stopExpirationTask();
        }
    }
}
