package com.youngbryanyu.burgerdb.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

/**
 * Class that represents the HTTP server used to communicate through HTTP
 * protocol with clients.
 */
public class HttpServerHandler {
    /**
     * IP address of localhost
     */
    private static final String LOCAL_HOST = "127.0.0.1";

    /**
     * Port that the HTTP server listens on
     */
    private int port;

    /**
     * Construct for {@link HttpServerHandler}
     * @param port
     */
    public HttpServerHandler(int port) {
        this.port = port;
    }

    /**
     * Starts the HTTP server
     */
    public void startServer() {
        /* Set up HTTP request handler */
        HttpRequestHandler requestHandler = (request, response, context) -> {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String body = EntityUtils.toString(entity);
                System.out.println("Received request body from HTTP client: " + body);
            }

            response.setStatusCode(HttpStatus.SC_OK);
            response.setEntity(new StringEntity("Response from server at " + port + "\n"));
        };

        try {
            /* Create HTTP server */
            HttpServer server = ServerBootstrap.bootstrap()
                    .setLocalAddress(InetAddress.getByName(LOCAL_HOST))
                    .setListenerPort(port)
                    .registerHandler("*", requestHandler)
                    .create();

            /* Start the HTTP */
            server.start();
            System.out.printf("HTTP Server started on port %d\n", port);
            server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException occurred while attempting to get local host IP: ");
            e.printStackTrace();
        } catch (IOException  e) {
            System.out.println("IOException occurred while starting server: ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException occurred while starting server: ");
            e.printStackTrace();
        }
    }
}
