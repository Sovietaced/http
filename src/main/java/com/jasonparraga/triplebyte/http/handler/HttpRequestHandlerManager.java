package com.jasonparraga.triplebyte.http.handler;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jasonparraga.triplebyte.http.HttpRequest;
import com.jasonparraga.triplebyte.http.HttpResponse;

public class HttpRequestHandlerManager {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandlerManager.class);

    private final Queue<HttpRequestHandler> requestHandlers = new LinkedList<>();

    /**
     * Registers a {@link HttpRequestHandler} at the front of the line.
     * @param handler
     */
    public void registerHandler(HttpRequestHandler handler) {
        requestHandlers.add(handler);
    }

    public HttpResponse handleRequest(HttpRequest request) {
        for (HttpRequestHandler handler : requestHandlers) {
            Optional<HttpResponse> maybeResponse;
            try {
                maybeResponse = handler.handleRequest(request);
            } catch (HttpRequestHandlerException e) {
                return HttpResponse.internalServerError(request, e);
            }

            if (maybeResponse.isPresent()) {

                log.info("{} handled request {} and returned {}",
                         handler.getClass().getSimpleName(), request,
                         maybeResponse.get().getStatus());

                return maybeResponse.get();
            }
        }

        // If none of the handlers can service the request...
        return HttpResponse.notFound(request);
    }
}
