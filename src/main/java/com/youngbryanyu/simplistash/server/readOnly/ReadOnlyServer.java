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
     * The number of current client connections.
     */
    private int currentConnections;
    /**
     * The port the server listens on.
     */
    private int port;
     /**
     * The max allowed server connections.
     */
    private int maxConnections;

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

        currentConnections = 0;
        port = DEFAULT_READ_ONLY_PORT;
        maxConnections = MAX_CONNECTIONS_READ_ONLY;
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
        /* Get custom port */
        String readOnlyPortString = System.getProperty("readOnlyPort");
        if (readOnlyPortString != null) {
            try {
                port = Integer.parseInt(readOnlyPortString);
            } catch (NumberFormatException e) {
                logger.debug("Invalid read only port, falling back to default: " + DEFAULT_READ_ONLY_PORT);
            }
        }

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);

            /* Bind to port and listen for connections */
            ChannelFuture f = bootstrap.bind(port).sync();
            logger.info("Read-only server started on port: " + port);

            /* Wait until server is closed */
            f.channel().closeFuture().sync();
        } finally {
            /* Cleanup */
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

     /**
     * Increment the number of server connections. Does nothing and returns false if
     * the max number of connections has been reached.
     * 
     * @return False if the max number of connections has been reached, true
     *         otherwise.
     */
    public synchronized boolean incrementConnections() {
        currentConnections++;
        if (currentConnections < maxConnections) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Decrement the number of server connections.
     */
    public synchronized void decrementConnections() {
        currentConnections--;
    }
}
