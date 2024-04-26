package com.youngbryanyu.simplistash.server;

import java.util.Deque;
import java.util.LinkedList;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.protocol.TokenParser;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Class that handles communication between the server and client. A new
 * independent client handler is spun up for each connection to the server.
 */
class ClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * The in-memory cache that stores data.
     */
    private final InMemoryCache cache;
    /**
     * The buffer holding all unprocessed data sent by the client.
     */
    private final StringBuilder buffer;
    /**
     * Tokens parsed from the buffer.
     */
    private final Deque<String> tokens;

    /**
     * Constructor for the client handler.
     * 
     * @param cache The in memory cache instance to store data in.
     */
    public ClientHandler(InMemoryCache cache) {
        this.cache = cache;
        buffer = new StringBuilder();
        tokens = new LinkedList<>();
    }

    /**
     * Called when a client connects and a new channel is opened for the client.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel());
        super.channelActive(ctx);
    }

    /**
     * Called when input data is received from the client's channel. Adds the data
     * to the client's buffer, parses tokens from the buffer, then handles any valid
     * commands formed by the tokens.
     * 
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String input = (String) msg;

        buffer.append(input);
        TokenParser.parseTokens(buffer, tokens);
        String response = CommandHandler.handleCommands(cache, tokens);

        if (response != null) {
            ctx.writeAndFlush(response);
        }
    }

    /**
     * Called when the client disconnects and their channel is closed.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel());
        super.channelInactive(ctx);
    }

    /**
     * Called when an exception is thrown in the channel and bubbles up to this
     * level in the call stack. Prints the stack trace of the exception, sends the
     * error message to the client, then closes the client's channel.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.printf("Error occurred in channel (%s)\n", ctx.channel());
        cause.printStackTrace();
        ctx.writeAndFlush(cause.getMessage()); // TODO: format errors in sendable form to client
        ctx.close();
    }
}