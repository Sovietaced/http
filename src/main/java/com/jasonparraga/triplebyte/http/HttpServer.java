package com.jasonparraga.triplebyte.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jasonparraga.triplebyte.http.handler.HttpRequestHandler;
import com.jasonparraga.triplebyte.http.handler.HttpRequestHandlerManager;

/**
 * An HTTP server that serves static files
 */
public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private final HttpRequestHandlerManager handlerManager = new HttpRequestHandlerManager();
    private final ServerSocket serverSocket;
    private volatile boolean running = false;
    // No multi threading for now...
    private final ExecutorService execService = Executors.newFixedThreadPool(1);

    /**
     * Constructor for the {@link HttpServier}
     * @param port the port to run the server on. Passing in 0 will allocate a port
     * automatically
     * @param path the path on the file system for which to serve files from
     * @throws IOException
     */
    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void registerHandler(HttpRequestHandler handler) {
        if (running) {
            throw new IllegalStateException("Cannot register new handler while server is running");
        }

        handlerManager.registerHandler(handler);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Starts up the server by starting a new worker.
     */
    public void run() {
        log.info("HTTP Server running on {}", serverSocket.getLocalSocketAddress());
        execService.submit(new ServerRunnable());
    }

    /**
     * Shuts down the server by killing the worker.
     */
    public void shutdown() {
        log.info("Shutting down the server.");
        execService.shutdown();
    }

    /**
     * Runnable server worker.
     */
    public class ServerRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {

                // Open Socket
                try (Socket clientSocket = serverSocket.accept();){
                    // Read in HTTP request
                    HttpRequest request = HttpRequest.of(clientSocket);

                    HttpResponse response = handlerManager.handleRequest(request);

                    clientSocket.getOutputStream().write(response.getBytes());

                } catch (Throwable e) {
                    log.error("Unexpected exception occurred. Shutting down.", e);
                    shutdown();
                }
            }
        }
    }
}
