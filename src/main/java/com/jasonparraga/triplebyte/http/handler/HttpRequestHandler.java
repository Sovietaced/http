package com.jasonparraga.triplebyte.http.handler;

import java.util.Optional;

import com.jasonparraga.triplebyte.http.HttpRequest;
import com.jasonparraga.triplebyte.http.HttpResponse;

/**
 * Basic contract for a module that will handle HTTP requests
 */
public interface HttpRequestHandler {

    Optional<HttpResponse> handleRequest(HttpRequest request) throws HttpRequestHandlerException;

}
