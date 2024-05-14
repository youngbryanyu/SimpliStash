package com.youngbryanyu.simplistash.server.client;

import java.util.Deque;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.exceptions.BrokenProtocolException;
import com.youngbryanyu.simplistash.exceptions.BufferOverflowException;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;
import com.youngbryanyu.simplistash.stash.Stash;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultMaxBytesRecvByteBufAllocator;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Class that handles communication between the server and client, and maintains
 * state for each client. A new client handler instance is created for each
 * connection to the server and will run on one of the NIO worker threads.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * The buffer holding all raw data sent by the client.
     */
    private final StringBuilder buffer;
    /**
     * Tokens parsed from the buffer.
     */
    private final Deque<String> tokens;
    /**
     * The command handler used to execute commands.
     */
    private final CommandHandler commandHandler;
    /**
     * The application logger.
     */
    private final Logger logger;
    /**
     * Whether the client is read-only.
     */
    private final boolean readOnly;

    /**
     * Constructor for the client handler.
     * 
     * @param commandHandler The command handler used to execute commands.
     * @param logger         The application logger to use.
     * @param readOnly       Whether or not the client is read-only.
     */
    @Autowired
    public ClientHandler(CommandHandler commandHandler, Logger logger, boolean readOnly) {
        this.commandHandler = commandHandler;
        this.logger = logger;
        this.readOnly = readOnly;
        buffer = new StringBuilder();
        tokens = new LinkedList<>();
    }

    /**
     * Called when a client connects and their channel is opened.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Client connected: %s", ctx.channel()));
        super.channelActive(ctx);
    }

    /**
     * Called when data is received from the client's channel. Adds the data to the
     * client's buffer, parses tokens from the buffer, then handles any full valid
     * commands formed by the tokens.
     * 
     * The maximum number of bytes that can be read in a single call to channelRead
     * is 65536, which is set in the class
     * {@link DefaultMaxBytesRecvByteBufAllocator}.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String input = (String) msg;
        buffer.append(input);
        parseTokens();
        String response = commandHandler.handleCommands(tokens, readOnly);

        if (response != null) {
            ctx.writeAndFlush(response);
        }
    }

    /**
     * Called when the client disconnects and their channel is closed.
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Client disconnected: %s", ctx.channel()));
        super.channelInactive(ctx);
    }

    /**
     * Called when an fatal exception is thrown in the channel and is not caught.
     * Sends the error message to the client, then closes the client's channel.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug(String.format("Error occurred in channel, disconnecting client: %s\n" +
                "- Error: %s", ctx.channel(), cause.getMessage()));
        ctx.writeAndFlush(ProtocolUtil.buildErrorResponse(cause.getMessage()));
        ctx.close();
    }

    /**
     * Parses tokens from the input buffer and stores them in the token deque. The
     * algorithm works in a length-prefixed fashion by finding the delimiter,
     * getting the size of the token to read, then reading that exact many bytes.
     * 
     * The format of each token should be like: <num_bytes><delimiter><token>
     * 
     * @param buffer The buffer holding unprocessed data from a client.
     * @param tokens The tokens that have been parsed from the client's buffer.
     * @throws BufferOverflowException If the client's buffer exceeds the maximum
     *                                 allowed size.
     * @throws BrokenProtocolException If the client's input doesn't follow the
     *                                 protocol.
     */
    private void parseTokens()
            throws BufferOverflowException, BrokenProtocolException {
        /* Check if buffer's size has exceeded the limit */
        if (buffer.length() > getMaxBufferSize()) {
            throw new BufferOverflowException();
        }

        String delim = ProtocolUtil.DELIM;
        int delimLength = delim.length();
        int lastEndIdx = 0; /* End of last token parsed */
        int delimIdx = -1;

        while ((delimIdx = buffer.indexOf(delim, lastEndIdx)) != -1) {
            /* Get token size */
            int size;
            try {
                size = Integer.parseInt(buffer.substring(lastEndIdx, delimIdx));
            } catch (NumberFormatException e) {
                throw new BrokenProtocolException(BrokenProtocolException.TOKEN_SIZE_INVALID_INTEGER, e);
            }

            /* Validate token size */
            if (size < 1) {
                throw new BrokenProtocolException(BrokenProtocolException.TOKEN_SIZE_OUT_OF_RANGE, null);
            }

            /* Get start and end indices of token */
            int startIdx = delimIdx + delimLength;
            int endIdx = startIdx + size;

            /* Check if buffer contains enough bytes */
            if (endIdx > buffer.length()) {
                break;
            }

            /* Add token to deque */
            String token = buffer.substring(startIdx, endIdx);
            tokens.addLast(token);
            lastEndIdx = endIdx;
        }

        /* Delete processed tokens from buffer */
        if (lastEndIdx > 0) {
            buffer.delete(0, lastEndIdx);
        }
    }
    
    /**
     * Returns the client's current tokens.
     * @return The client's tokens.
     */
    public Deque<String> getTokens() {
        return tokens;
    }

    /**
     * Returns the client's current buffer.
     * @return The client's current buffer.
     */
    public StringBuilder getBuffer() {
        return buffer;
    }

    /**
     * Returns the max buffer size each client can have. This is to prevent the
     * client from using up too much memory. For cushion, we allocate 3 times the
     * size of the max key size plus max value size to account for other arguments.
     * 
     * @return The a client's max buffer size
     */
    public static int getMaxBufferSize() {
        return 3 * (Stash.MAX_KEY_LENGTH + Stash.MAX_VALUE_LENGTH);
    }
}