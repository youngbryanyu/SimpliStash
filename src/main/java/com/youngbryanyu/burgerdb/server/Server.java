package com.youngbryanyu.burgerdb.server;

/**
 * The class encapsulating the servers to communicate with clients.
 */
public class Server {
    /**
     * Port to use for HTTP server.
     */
    private static final int HTTP_PORT = 80;

    /**
     * Port to use for TCP server.
     */
    private static final int TCP_PORT = 3000;

    /**
     * Main method that the program runs on.
     * @param args Command line args.
     */
    public static void main(String[] args) {
        TcpServerHandler tcpServer = new TcpServerHandler(TCP_PORT);
        HttpServerHandler httpServer = new HttpServerHandler(HTTP_PORT);
        
        /* Start TCP and HTTP servers in new threads */
        new Thread(tcpServer::startServer).start();
        new Thread(httpServer::startServer).start();
    }
}
