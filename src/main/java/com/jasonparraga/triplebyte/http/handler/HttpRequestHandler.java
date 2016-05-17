package com.jasonparraga.triplebyte.http.handler;

import java.util.Optional;

import com.jasonparraga.triplebyte.http.HttpRequest;
import com.jasonparraga.triplebyte.http.HttpResponse;

public interface HttpRequestHandler {

    Optional<HttpResponse> handleRequest(HttpRequest request) throws HttpRequestHandlerException;

}
