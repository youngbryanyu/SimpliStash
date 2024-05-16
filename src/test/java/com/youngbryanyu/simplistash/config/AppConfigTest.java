package com.youngbryanyu.simplistash.config;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.youngbryanyu.simplistash.server.client.ClientHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Tests for the spring IoC/DI config class.
 */
@SpringJUnitConfig(AppConfig.class)
public class AppConfigTest {
    /**
     * The application context.
     */
    @Autowired
    private ApplicationContext context;
    /**
     * The logger.
     */
    @Autowired
    private Logger logger;

    /**
     * Tests creating the logger bean.
     */
    @Test
    public void testLoggerBean() {
        assertTrue(logger instanceof Logger);
    }

    /**
     * Tests creating the DB bean.
     */
    @Test
    public void testDBBean_scope() {
        DB db1 = context.getBean(DB.class);
        DB db2 = context.getBean(DB.class);
        assertNotSame(db1, db2);
        assertTrue(db1 instanceof DB);
        assertTrue(db2 instanceof DB);
    }

    /**
     * Tests creating the read only client handler bean.
     */
    @Test
    public void testReadOnlyClientHandlerBean() {
        ClientHandler readOnlyHandler = context.getBean(AppConfig.READ_ONLY_CLIENT_HANDLER, ClientHandler.class);
        assertTrue(readOnlyHandler instanceof ClientHandler);
    }

    /**
     * Tests creating the primary client handler bean.
     */
    @Test
    public void testPrimaryClientHandlerBean() {
        ClientHandler primaryHandler = context.getBean(AppConfig.PRIMARY_CLIENT_HANDLER, ClientHandler.class);
        assertTrue(primaryHandler instanceof ClientHandler);
    }

    /**
     * Tests creating the default nio event loop group bean.
     */
    @Test
    public void testNioEventLoopGroup() {
        EventLoopGroup eventLoopGroup = context.getBean(EventLoopGroup.class);
        assertTrue(eventLoopGroup instanceof NioEventLoopGroup);
    }

    /**
     * Tests creating the primary nio event loop group with a single worker thread
     * bean.
     */
    @Test
    public void testNioEventLoopGroup_singleThread() {
        EventLoopGroup eventLoopGroup = context.getBean(AppConfig.SINGLE_THREADED_NIO_EVENT_LOOP_GROUP,
                EventLoopGroup.class);
        assertTrue(eventLoopGroup instanceof NioEventLoopGroup);
    }

    /**
     * Tests creating the server bootstrap bean.
     */
    @Test
    public void testServerBootstrap() {
        ServerBootstrap serverBootstrap = context.getBean(ServerBootstrap.class);
        assertTrue(serverBootstrap instanceof ServerBootstrap);
    }

    // TODO: add tests for new beans

}
