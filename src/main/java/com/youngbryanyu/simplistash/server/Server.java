package com.youngbryanyu.simplistash.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cache.InMemoryCache;

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
 * The server which listens for incoming client connections.
 */
@Component
public class Server {
    /**
     * The port that the server should listen on.
     */
    private final int port;
    /**
     * The in-memory cache that clients may write to.
     */
    private final InMemoryCache cache; // TODO: replace with stash manager

    /**
     * Constructor for the server.
     * 
     * @param port  The port to listen on.
     * @param cache The in-memory cache to store data to.
     */
    @Autowired
    public Server(InMemoryCache cache) {
        this.port = 3000; // TODO: inject port 
        this.cache = cache;
    }

    /**
     * Starts the server. Creates boss threads to accept incoming connections and
     * worker threads to handle I/O from connected clients. Each worker thread runs
     * an event loop that handles I/O in a non-blocking fashion. Sets up the
     * pipeline of handlers for each client channel.
     * 
     * @throws Exception If the server fails to start.
     */
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

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
                                    new ClientHandler(cache)); /* Spin up a client handler for each connection */
                        }
                    });

            /* Bind to port and listen for connections, then wait until server is closed */
            ChannelFuture f = bootstrap.bind(port).sync();
            System.out.println("Server started on port: " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
