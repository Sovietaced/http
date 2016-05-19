package com.jasonparraga.triplebyte.http.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jasonparraga.triplebyte.http.HttpServer;
import com.jasonparraga.triplebyte.http.handler.CgiHttpRequestHandler;
import com.jasonparraga.triplebyte.http.handler.StaticFileHttpRequestHandler;

public class Example {

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(8080);

        Path staticPath = Paths.get("/tmp/test");
        StaticFileHttpRequestHandler staticHandler = new StaticFileHttpRequestHandler(staticPath);
        server.registerHandler(staticHandler);

        Path execPath = Paths.get("/tmp/exec");
        CgiHttpRequestHandler cgiHandler = new CgiHttpRequestHandler(execPath);
        server.registerHandler(cgiHandler);

        server.run();
    }
}
