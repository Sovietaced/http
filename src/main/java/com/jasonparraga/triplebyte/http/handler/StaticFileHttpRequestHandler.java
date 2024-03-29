package com.jasonparraga.triplebyte.http.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.jasonparraga.triplebyte.http.HttpRequest;
import com.jasonparraga.triplebyte.http.HttpResponse;

public class StaticFileHttpRequestHandler implements HttpRequestHandler {

    private final Path fileSystemBasePath;

    public StaticFileHttpRequestHandler(Path fileSystemBasePath) {
        this.fileSystemBasePath = fileSystemBasePath;
    }

    @Override
    public Optional<HttpResponse> handleRequest(HttpRequest request) throws HttpRequestHandlerException {
        Path fileSystemPath = HttpRequestHandlerUtils.getFileSystemPath(fileSystemBasePath, request.getPath());

        byte[] fileContent = getContentForFile(fileSystemPath);

        if (fileContent == null) {
            return Optional.of(HttpResponse.notFound(request));
        } else {
            return Optional.of(HttpResponse.ok(request, fileContent));
        }
    }

    private byte[] getContentForFile(Path path) throws HttpRequestHandlerException {
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new HttpRequestHandlerException("Failed to read bytes.", e);
            }
        }

        return null;
    }

}
