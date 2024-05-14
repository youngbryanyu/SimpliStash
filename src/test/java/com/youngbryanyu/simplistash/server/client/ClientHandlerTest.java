package com.youngbryanyu.simplistash.server.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Deque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.exceptions.BrokenProtocolException;
import com.youngbryanyu.simplistash.exceptions.BufferOverflowException;

import io.netty.channel.ChannelHandlerContext;

/**
 * Unit tests for the client handler.
 */
public class ClientHandlerTest {
    /**
     * The mocked command handler.
     */
    @Mock
    private CommandHandler mockCommandHandler;
    /**
     * The mocked logger.
     */
    @Mock
    private Logger mockLogger;
    /**
     * The mocked channel handler context.
     */
    @Mock
    private ChannelHandlerContext mockCtx;
    /**
     * Argument captor.
     */
    @Captor
    private ArgumentCaptor<String> captor;

    /**
     * The client handler under test.
     */
    private ClientHandler clientHandler;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        clientHandler = new ClientHandler(mockCommandHandler, mockLogger, false);
    }

    /**
     * Test {@link ClientHandler#channelActive(ChannelHandlerContext)}.
     */
    @Test
    void testChannelActive() throws Exception {
        when(mockCtx.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        clientHandler.channelActive(mockCtx);
        verify(mockLogger).debug(anyString());
    }

    /**
     * Test {@link ClientHandler#channelRead(ChannelHandlerContext, Object)}.
     */
    @Test
    void testChannelRead() throws Exception {
        when(mockCommandHandler.handleCommands(any(), eq(false))).thenReturn("OK");
        clientHandler.channelRead(mockCtx, "5\r\nhello");
        verify(mockCtx).writeAndFlush("OK");
    }

    /**
     * Test {@link ClientHandler#channelRead(ChannelHandlerContext, Object)} when
     * the response is null from the command handler indicating no full command was
     * executed.
     */
    @Test
    void testChannelRead_nullCommandHandlerResponse() throws Exception {
        when(mockCommandHandler.handleCommands(any(), eq(false))).thenReturn(null);
        clientHandler.channelRead(mockCtx, "5\r\nhello");
        verify(mockCtx, never()).writeAndFlush(anyString());
    }

    /**
     * Test {@link ClientHandler#channelInactive(ChannelHandlerContext)}.
     */
    @Test
    void testChannelInactive() throws Exception {
        when(mockCtx.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        clientHandler.channelInactive(mockCtx);
        verify(mockLogger).debug(anyString());
    }

    /**
     * Test {@link ClientHandler#exceptionCaught(ChannelHandlerContext, Throwable)}.
     */
    @Test
    void testExceptionCaught() {
        Throwable cause = new Throwable("Test Exception");
        when(mockCtx.channel()).thenReturn(mock(io.netty.channel.Channel.class));

        clientHandler.exceptionCaught(mockCtx, cause);

        verify(mockLogger).debug(anyString());
        verify(mockCtx).writeAndFlush(anyString());
        verify(mockCtx).close();
    }

    /**
     * Test {@link ClientHandler#parseTokens()}.
     */
    @Test
    void testParseTokens_validData() throws Exception {
        /* Setup */
        when(mockCommandHandler.handleCommands(any(), anyBoolean())).thenReturn("response1");
        String input = "5\r\nhello5\r\npizza";

        /* Call method */
        clientHandler.channelRead(mockCtx, input);

        /* Get tokens parsed */
        Deque<String> tokens = clientHandler.getTokens();
        StringBuilder buffer = clientHandler.getBuffer();

        /* Check assertions */
        assertEquals(2, tokens.size());
        assertEquals(0, buffer.length());
        assertEquals("hello", tokens.pollFirst());
        assertEquals("pizza", tokens.pollFirst());
    }

    /**
     * Test {@link ClientHandler#parseTokens()} when the buffer overflows.
     */
    @Test
    void testParseTokens_bufferOverflow() {
        StringBuilder largeInput = new StringBuilder();
        for (int i = 0; i < ClientHandler.getMaxBufferSize() + 1; i++) {
            largeInput.append("a");
        }

        assertThrows(BufferOverflowException.class, () -> {
            clientHandler.channelRead(mockCtx, largeInput.toString());
        });
    }

    /**
     * Test {@link ClientHandler#parseTokens()} when the protocol is broken by the
     * client.
     */
    @Test
    void testParseTokens_brokenProtocol() {
        String input = "5--\r\nhe";
        assertThrows(BrokenProtocolException.class, () -> {
            clientHandler.channelRead(mockCtx, input);
        });
    }

    /**
     * Test {@link ClientHandler#parseTokens()} when the token size is out of range
     * in the protocol.
     */
    @Test
    void testParseTokens_tokenSizeOutOfRange() {
        String input = "-1\r\nhe";
        assertThrows(BrokenProtocolException.class, () -> {
            clientHandler.channelRead(mockCtx, input);
        });
    }

    /**
     * Test {@link ClientHandler#parseTokens()} when there's not enough bytes in the
     * buffer yet based on the token size parsed.
     */
    @Test
    void testParseTokens_notEnoughBytes() throws Exception {
        /* Setup */
        when(mockCommandHandler.handleCommands(any(), anyBoolean())).thenReturn("response1");
        String input = "5\r\nhello100\r\npizza";

        /* Call method */
        clientHandler.channelRead(mockCtx, input);

        /* Get tokens parsed */
        Deque<String> tokens = clientHandler.getTokens();
        StringBuilder buffer = clientHandler.getBuffer();

        /* Check assertions */
        assertEquals(1, tokens.size());
        assertEquals("100\r\npizza", buffer.toString());
        assertEquals("hello", tokens.pollFirst());
    }

    /**
     * Test {@link ClientHandler#parseTokens()} when there's no delim.
     */
    @Test
    void testParseTokens_noDelim() throws Exception {
        /* Setup */
        when(mockCommandHandler.handleCommands(any(), anyBoolean())).thenReturn("response1");
        String input = "5";

        /* Call method */
        clientHandler.channelRead(mockCtx, input);

        /* Get tokens parsed */
        Deque<String> tokens = clientHandler.getTokens();
        StringBuilder buffer = clientHandler.getBuffer();

        /* Check assertions */
        assertEquals(0, tokens.size());
        assertEquals("5", buffer.toString());
    }
}
