package com.jasonparraga.triplebyte.http.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jasonparraga.triplebyte.http.HttpServer;
import com.jasonparraga.triplebyte.http.handler.StaticFileHttpRequestHandler;

public class Example {

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(8080);

        Path path = Paths.get("/tmp/test");
        StaticFileHttpRequestHandler staticHandler = new StaticFileHttpRequestHandler(path);
        server.registerHandler(staticHandler);

        server.run();
    }
}
