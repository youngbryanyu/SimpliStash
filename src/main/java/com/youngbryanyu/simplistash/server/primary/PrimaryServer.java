package com.youngbryanyu.simplistash.server.primary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.replica.ReplicaCommand;
import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;
import com.youngbryanyu.simplistash.utils.IOFactory;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

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
     * The IO factory.
     */
    private final IOFactory ioFactory;
    /**
     * The output writer.
     */
    private PrintWriter out;
    /**
     * The socket to connect to the master if the current node is a read-replica.
     */
    private Socket socket;

    /**
     * Constructor for the server.
     * 
     * @param cache The in-memory cache to store data to.
     */
    @Autowired
    public PrimaryServer(EventLoopGroup bossGroup,
            @Qualifier(AppConfig.SINGLE_THREADED_NIO_EVENT_LOOP_GROUP) EventLoopGroup workerGroup,
            ServerBootstrap bootstrap,
            PrimaryChannelInitializer channelInitializer,
            KeyExpirationManager keyExpirationManager,
            Logger logger,
            IOFactory ioFactory) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.bootstrap = bootstrap;
        this.channelInitializer = channelInitializer;
        this.keyExpirationManager = keyExpirationManager;
        this.logger = logger;
        this.ioFactory = ioFactory;

        currentConnections = 0;
        port = DEFAULT_PRIMARY_PORT;
        maxConnections = MAX_CONNECTIONS_PRIMARY;
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
        /* Get custom port */
        String primaryPortString = System.getProperty("primaryPort");
        if (primaryPortString != null) {
            try {
                port = Integer.parseInt(primaryPortString);
            } catch (NumberFormatException e) {
                logger.debug("Invalid primary port, falling back to default: " + DEFAULT_PRIMARY_PORT);
            }
        }

        /* Set up periodic task to expire TTLed keys */
        keyExpirationManager.startExpirationTask(workerGroup);

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);

            /* Bind to port and listen for connections */
            ChannelFuture f = bootstrap.bind(port).sync();
            logger.info("Primary server started on port: " + port);

            /* Deterine if we should register as read replica */
            String masterIp = System.getProperty("masterIp");
            String masterPortStr = System.getProperty("masterPort");
            if (masterIp != null && !masterIp.isEmpty() && masterPortStr != null && !masterPortStr.isEmpty()) {
                try {
                    /* Register node as read replica */
                    int masterPort = Integer.parseInt(masterPortStr);
                    registerAsReplica(masterIp, masterPort, InetAddress.getLocalHost().getHostAddress(), port);
                } catch (NumberFormatException e) {
                    logger.warn(
                            "Invalid master port, setting up node as its own master node instead of read-replica...");
                }
            }

            /* Wait until server is closed */
            f.channel().closeFuture().sync();
        } finally {
            /* Cleanup */
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            keyExpirationManager.stopExpirationTask();
        }
    }

    /**
     * Registers the current node as a read-replica with the master by sending the
     * REPLICA <ip> <port> command to the master.
     * 
     * @param masterIp   The master's IP.
     * @param masterPort The master's port.
     * @param string     The current node's IP.
     * @param port       The current node's port
     * @throws IOException
     */
    private void registerAsReplica(String masterIp, int masterPort, String ip, int port) throws IOException {
        /* Send replica command to server */
        String command = ProtocolUtil.encode(ReplicaCommand.NAME, List.of(ip, Integer.toString(port)), false,
                Collections.emptyMap());
        try {
            socket = ioFactory.createSocket(masterIp, masterPort);
            out = ioFactory.createWriter(socket);
            out.print(command);
            out.flush();

            /* Set max connections to 1 as a read replica so only the master can connect */
            maxConnections = REPLICA_PRIMARY_CONNECTION_LIMIT;

            logger.info(
                    String.format("Node registered as a read-replica for master node at: %s/%d", masterIp, masterPort));
        } catch (IOException e) {
            logger.warn("Failed to contact master node to set up read-replication.");
            throw e;
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
        if (currentConnections < maxConnections) {
            currentConnections++;
            return true;
        } else {
            currentConnections++;
            return false;
        }
    }

    /**
     * Decrement the number of server connections.
     */
    public synchronized void decrementConnections() {
        currentConnections--;
    }

    /**
     * Returns the port the server is listening on.
     * 
     * @return The port the server is listening on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the max number of connections.
     * 
     * @return The max number of connections.
     */
    public int getMaxConnections() {
        return maxConnections;
    }
}
