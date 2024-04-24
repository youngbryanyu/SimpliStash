package com.youngbryanyu.simplistash.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.commands.ProtocolFormatter;

import javafx.util.Pair;

/**
 * The server handler which handles all communication with clients. Runs on a
 * single thread using NIO (non-blocking IO). Is responsible for communicating
 * with the in-memory cache.
 */
public class ServerHandler {
    /**
     * Channel selector used to select client channels to perform I/O with.
     */
    private final Selector selector;
    /**
     * The server socket channel which listens for connections.
     */
    private final ServerSocketChannel serverSocketChannel;
    /**
     * The key value store being used
     */
    private final InMemoryCache cache;
    /**
     * Flag indicating whether or not the server is running.
     */
    private volatile boolean running;
    /**
     * A map of each open client socket channel to a buffer of what has been
     * accumulated over input from the client. We use a StringBuilder to fill up the
     * buffer and store chunks of delimited input in a Deque so we don't need to
     * re-use indexOf to check for arguments.
     */
    private final Map<SocketChannel, Pair<StringBuilder, Deque<String>>> clientBuffersAndTokens;

    /**
     * Constructor for {@link ServerHandler}. Configures the server to be
     * non-blocking on I/O operations. Throws an {@link IOException} if one occurs
     * while initializing the server.
     * 
     * @param port The port that the server listens on.
     */
    public ServerHandler(int port, InMemoryCache cache) throws IOException {
        this.selector = Selector.open();

        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.running = false;
        this.cache = cache;
        clientBuffersAndTokens = new HashMap<>();
    }

    /**
     * Starts the server, listens for connections, and handles I/O with clients.
     */
    public void startServer() {
        this.running = true;
        System.out.println("[LOG] Server started on port: " + serverSocketChannel.socket().getLocalPort());

        while (running) {
            /* Select keys of channels ready for I/O */
            try {
                selector.select(); /* Blocking operation */
            } catch (IOException e) {
                System.out.println("IOException occurred while server was selecting channels for I/O:");
                e.printStackTrace();
            }

            /* Gracefully exit if selector is closed */
            if (!selector.isOpen()) {
                break;
            }

            /* Iterate over selected channels and perform I/O */
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    handleAccept();
                } else if (key.isReadable()) {
                    handleReadAndWrite(key);
                }
                iter.remove();
            }
        }
    }

    /**
     * Handles new incoming connections to the server.
     */
    private void handleAccept() {
        try {
            SocketChannel clientChannel = serverSocketChannel.accept();

            if (clientChannel != null) {
                clientChannel.configureBlocking(false); /* Configure I/O operations to be non-blocking */
                clientChannel.register(selector, SelectionKey.OP_READ);
                System.out.println("[LOG] Accepted new connection from client: " + clientChannel.getRemoteAddress());
            }
        } catch (IOException e) {
            System.out.println("IOException occurred in server while handling incoming client connection:");
            e.printStackTrace();
        }
    }

    /**
     * Reads input from the client channel, processes the data, and
     * sends a response back to the client.
     * 
     * @param key The selection key of the channel when it was registered with the
     *            selector.
     */
    private void handleReadAndWrite(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Pair<StringBuilder, Deque<String>> clientBuffer = clientBuffersAndTokens.computeIfAbsent(clientChannel,
                k -> {
                    return new Pair<>(new StringBuilder(), new LinkedList<String>());
                });
        ByteBuffer readBuffer = ByteBuffer.allocate(ProtocolFormatter.MAX_INPUT_LENGTH);

        try {
            /* Read data into buffer */
            int numRead = clientChannel.read(readBuffer);

            /* Check if connection was closed by client */
            if (numRead == -1) {
                System.out.println("[LOG] Connection closed by client: " + clientChannel.getRemoteAddress());
                closeClientChannel(key, clientChannel);
                return;
            }

            /* Process data from buffer */
            readBuffer.flip();
            byte[] data = new byte[readBuffer.limit()];
            readBuffer.get(data);
            String input = new String(data, StandardCharsets.UTF_8);
            clientBuffer.getKey().append(input);
            System.out.printf("[LOG] Received from client (%s): %s", clientChannel.getRemoteAddress(), input);

            /* Parse and handle command from data */
            String response = CommandHandler.handleCommands(input, cache, clientBuffer);

            /* Respond to client if a command was handled */
            if (response != null) {
                ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                clientChannel.write(responseBuffer);
                System.out.printf("[LOG] Send to client (%s): %s", clientChannel.getRemoteAddress(), response);
            }
        } catch (IOException e) {
            System.out.println("IOException occurred while reading or writing to client.");

            /* Close client channel and invalidate its key */
            closeClientChannel(key, clientChannel);
        }
    }

    /**
     * Closes the client channel and invalidates its key used by the selector.
     * 
     * @param key           The channel key used by the selector.
     * @param clientChannel The client's channel used to communicate with the
     *                      server.
     */
    private void closeClientChannel(SelectionKey key, SocketChannel clientChannel) {
        clientBuffersAndTokens.remove(clientChannel);
        key.cancel();

        try {
            clientChannel.close();
        } catch (IOException e) {
            System.out.println("IOException occurred while closing client channel:");
            e.printStackTrace();
        }
    }

    /**
     * Stops the server. Sets `running` to false. Closes the selector which
     * will cause a {@link ClosedSelectorException} to be thrown if the selector is
     * being used in the server loop. Closes the server socket channel.
     */
    public void stopServer() {
        running = false;

        if (selector != null) {
            try {
                selector.close();
                serverSocketChannel.close();
            } catch (IOException e) {
                System.out.println("IOException occurred while closing server: ");
                e.printStackTrace();
            }
        }
    }
}
