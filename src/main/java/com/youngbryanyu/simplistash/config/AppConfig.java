package com.youngbryanyu.simplistash.config;

import java.util.concurrent.ThreadFactory;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.server.client.ClientHandler;
import com.youngbryanyu.simplistash.server.primary.PrimaryServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

/**
 * Dependency injection configuration class.
 */
@Configuration
@ComponentScan(basePackages = "com.youngbryanyu.simplistash")
public class AppConfig {
    /**
     * Creates a singleton logger.
     * 
     * @return The logger to use.
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("logger");
    }

    /**
     * Creates a new instance of an off-heap in-memory DB.
     * 
     * @return An off-heap in-memory DB.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DB db() {
        return DBMaker.memoryDirectDB().make();
    }

    /**
     * Name of the read only client handler beans.
     */
    public static final String READ_ONLY_CLIENT_HANDLER = "readOnlyClientHandler";

    /**
     * Creates an instance of a client handler with read-only permissions.
     * 
     * @param commandHandler The command handler.
     * @param logger         The application logger.
     * @return A new instance of a client handler.
     */
    @Bean(READ_ONLY_CLIENT_HANDLER)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ClientHandler readOnlyClientHandler(CommandHandler commandHandler, Logger logger) {
        return new ClientHandler(commandHandler, logger, true);
    }

    /**
     * Name of the primary client handler beans.
     */
    public static final String PRIMARY_CLIENT_HANDLER = "primaryClientHandler";

    /**
     * Creates an instance of a client handler with both read and write permissions.
     * 
     * @param commandHandler The command handler.
     * @param logger         The application logger.
     * @return A new instance of a client handler.
     */
    @Bean(PRIMARY_CLIENT_HANDLER)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ClientHandler primaryClientHandler(CommandHandler commandHandler, Logger logger) {
        return new ClientHandler(commandHandler, logger, false);
    }

    /**
     * Creates an instance of a default netty nio event loop group.
     * 
     * @return An instance of a default netty nio event loop group.
     */
    @Bean
    @Primary
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventLoopGroup eventLoopGroup() {
        return new NioEventLoopGroup();
    }

    /**
     * Name of a bean for the single threaded nio event loop group.
     */
    public static final String SINGLE_THREADED_NIO_EVENT_LOOP_GROUP = "singleThreadedNioEventLoopGroup";

    /**
     * Creates a singleton instance of a single threaded netty nio event loop group
     * with thread affinity used for the primary server's worker group.
     * 
     * @return A singleton instance of a single threaded netty nio event loop group.
     */
    @Bean(SINGLE_THREADED_NIO_EVENT_LOOP_GROUP)
    public EventLoopGroup nioEventLoopGroup_singleThread() {
        ThreadFactory threadFactory = new AffinityThreadFactory("atf_wrk", AffinityStrategies.DIFFERENT_CORE);
        return new NioEventLoopGroup(PrimaryServer.NUM_WORKER_THREADS, threadFactory);
    } // TODO: remove if deleting read-only server

    /**
     * Creates an instance of a default netty server bootstrap.
     * 
     * @return An instance of a default netty server bootstrap.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ServerBootstrap serverBootstrap() {
        return new ServerBootstrap();
    }
}

// TODO: add unit tests for new methods