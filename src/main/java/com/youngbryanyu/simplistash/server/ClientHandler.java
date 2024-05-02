package com.youngbryanyu.simplistash.server;

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
 * connection to the server. The client handler will run on one of the NIO
 * worker threads.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * The buffer holding all unprocessed data sent by the client.
     */
    private final StringBuilder buffer;
    /**
     * Tokens parsed from the buffer.
     */
    private final Deque<String> tokens;
    /**
     * The command handler used to execute commands sent by the client.
     */
    private final CommandHandler commandHandler;
    /**
     * The application logger.
     */
    private final Logger logger;
    /**
     * Whether the client is read-only and can only perform read-only actions.
     */
    private final boolean readOnly;

    /**
     * Constructor for the client handler.
     * 
     * @param commandHandler The command handler used to execute commands.
     * @param logger         The application logger to use.
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
     * Called when a client connects and a new channel is opened for the client.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Client connected: %s", ctx.channel()));
        super.channelActive(ctx);
    }

    /**
     * Called when input data is received from the client's channel. Adds the data
     * to the client's buffer, parses tokens from the buffer, then handles any valid
     * commands formed by the tokens.
     * 
     * The maximum number of bytes that can be read in a single call to channelRead
     * is 65536, which is set in the class
     * {@link DefaultMaxBytesRecvByteBufAllocator}.
     * 
     * @throws Exception If an exception occurs while reading or performing any
     *                   processing from this method.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String input = (String) msg;

        buffer.append(input);
        parseTokens();
        String response = commandHandler.handleCommands(tokens, readOnly);

        // TODO: think about guarding case when client's output buffer gets too big

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
        logger.debug(String.format("Client disconnected: %s", ctx.channel()));
        super.channelInactive(ctx);
    }

    /**
     * Called when an exception is thrown in the channel and it bubbles up to this
     * level in the call stack. Prints the stack trace of the exception, sends the
     * error message to the client, then closes the client's channel.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug(String.format("Error occurred in channel, disconnecting client (%s) --> %s", ctx.channel(),
                cause.getMessage()));
        logger.error("Error stack trace: ", cause);
        ctx.writeAndFlush(ProtocolUtil.buildErrorResponse(cause.getMessage()));
        ctx.close();
    }

    /**
     * Parses tokens from the input buffer and stores them in the token deque. The
     * algorithm works in a length-prefixed fashion by finding the delimiter,
     * getting the size of the token to read, then reading that exact many bytes.
     * Everything in the DB is treated as a string.
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
        /* Throw exception if buffer's size has exceeded the allowable limit */
        if (buffer.length() > getMaxBufferSize()) {
            throw new BufferOverflowException("Input buffer has overflowed");
        }

        String delim = ProtocolUtil.DELIM;
        int delimLength = delim.length();
        int delimIdx = -1;

        while ((delimIdx = buffer.indexOf(delim)) != -1) {
            /* Get the size of the token */
            int size;
            try {
                size = Integer.parseInt(buffer.substring(0, delimIdx));
            } catch (NumberFormatException e) {
                throw new BrokenProtocolException("The token size is not a valid integer. Disconnecting...", e);
            }

            /* Ensure the size of the token is at least 1 */
            if (size < 1) {
                throw new BrokenProtocolException("The token size must be at least 1. Disconnecting...", null);
            }

            /* Get the start and end indices of the token */
            int startIdx = delimIdx + delimLength;
            int endIdx = startIdx + size;

            /*
             * Break if there aren't enough values in the buffer, there may be more that
             * that haven't been sent from the client yet over the TCP connection stream
             */
            if (endIdx > buffer.length()) {
                break;
            }

            /* Add the token to the deque and remove it from the buffer */
            String token = buffer.substring(startIdx, endIdx);
            tokens.addLast(token);
            buffer.delete(0, endIdx);
        }
    }

    /**
     * Returns the max buffer size each client can have. This is to prevent the
     * client from using up too much memory. If the max buffer size is reached for a
     * client, they will be disconnected.
     * 
     * The number of bytes in the longest command is approximately:
     * - longest_command = <command> + <max_key_size> + <max_value_size> + <args>
     * 
     * Assume the worst case where all commands sent by the client are of
     * longest_command size. In the edge case that 90% of command 1 is sent, then
     * 100% of command 2 is sent, command 1 won't be processed until the data from
     * command 2 is read into the buffer. We don't want to exit with an buffer
     * overflow error here since this is valid. Thus we should allocated 2 times the
     * size of longest_command. As a generous cushion, we allocate 3 times the size
     * of longest_command to account for other arguments.
     * 
     * @return The max buffer size a client can have.
     */
    private static int getMaxBufferSize() {
        return 3 * (Stash.MAX_KEY_SIZE + Stash.MAX_VALUE_SIZE);
    }

    /**
     * Returns whether or not the client handler is can only perform read
     * operations.
     * 
     * @return True if the client is read-only, false otherwise.
     */
    public boolean isReadOnly() {
        return readOnly;
    }
}