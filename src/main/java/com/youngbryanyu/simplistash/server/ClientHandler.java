package com.youngbryanyu.simplistash.server;

import java.util.Deque;
import java.util.LinkedList;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.protocol.ProtocolFormatter;
import com.youngbryanyu.simplistash.protocol.TokenParser;
import com.youngbryanyu.simplistash.util.ConsoleColors;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultMaxBytesRecvByteBufAllocator;

/**
 * Class that handles communication between the server and client. A new
 * independent client handler is spun up for each connection to the server. The
 * client handler will run on one of the NIO worker threads.
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
        System.out.printf("[%sINFO%s] Client connected: %s\n", ConsoleColors.BLUE, ConsoleColors.RESET, ctx.channel());
        super.channelActive(ctx);
    }

    /**
     * Called when input data is received from the client's channel. Adds the data
     * to the client's buffer, parses tokens from the buffer, then handles any valid
     * commands formed by the tokens.
     * 
     * The maximum number of bytes that can be read in a single call to channelRead
     * is 65536, which is set in the class
     * {@link DefaultMaxBytesRecvByteBufAllocator}. This size helps ensure that no
     * client takes up too much memory when sending data, and that processing and
     * buffer size checking in {@link TokenParser#parseTokens} will be done at most
     * bytes.
     * 
     * @throws Exception If an exception occurs while reading or performing any
     *                   processing from this method.
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
     * 
     * @throws exception If any exception is thrown while closing the client
     *                   channel.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("[%sINFO%s] Client disconnected: %s\n", ConsoleColors.BLUE, ConsoleColors.RESET,
                ctx.channel());
        super.channelInactive(ctx);
    }

    /**
     * Called when an exception is thrown in the channel and bubbles up to this
     * level in the call stack. Prints the stack trace of the exception, sends the
     * error message to the client, then closes the client's channel.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.printf("[%sERROR%s] Error occurred in channel (%s): %s\n", ConsoleColors.RED, ConsoleColors.RESET,
                ctx.channel(), cause.getMessage());
        ctx.writeAndFlush(ProtocolFormatter.buildErrorResponse(cause.getMessage()));
        ctx.close();
    }
}