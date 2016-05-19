package com.jasonparraga.com.triplebyte.http;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.jasonparraga.triplebyte.http.HttpServer;
import com.jasonparraga.triplebyte.http.handler.StaticFileHttpRequestHandler;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * Integration style tests for the HttpServer
 */
public class HttpServerIntegrationTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();
    private HttpServer httpServer;
    private String baseUrl;
    private Path path;

    @Before
    public void setup() throws IOException {
        path = Paths.get(folder.getRoot().getAbsolutePath());
        httpServer = new HttpServer(0);
        httpServer.registerHandler(new StaticFileHttpRequestHandler(path));
        httpServer.run();
        baseUrl = String.format("http://127.0.0.1:%d", httpServer.getPort());
    }

    @After
    public void teardown() {
        httpServer.shutdown();
    }


    @Test
    public void testFound() throws Exception {
        // Create a simple file in the temporary folder
        Path newFilePath = path.resolve("good.txt");
        Files.createFile(newFilePath);
        Files.write(newFilePath, "test".getBytes());

        HttpResponse<String> response = Unirest.get(baseUrl + "/good.txt").asString();

        assertThat("Should result in a 200 OK", response.getStatus(), Matchers.is(200));
        assertThat("Body should be file content", response.getBody(), Matchers.is("test"));
    }

    @Test
    public void testFoundSubDirectory() throws Exception {
        // Create a simple file in the temporary folder
        Path newDir = path.resolve("sub");
        Path newFilePath = path.resolve("sub/good.txt");
        // Generare new sub directory and file
        Files.createDirectory(newDir);
        Files.createFile(newFilePath);
        Files.write(newFilePath, "test".getBytes());

        HttpResponse<String> response = Unirest.get(baseUrl + "/sub/good.txt").asString();

        assertThat("Should result in a 200 OK", response.getStatus(), Matchers.is(200));
        assertThat("Body should be file content", response.getBody(), Matchers.is("test"));
    }

    @Test
    public void testNotFound() throws Exception {
        assertThat("Should result in a 404 NOT FOUND",
                   Unirest.get(baseUrl + "/bad.txt").asString().getStatus(),
                   Matchers.is(404));
    }
}
