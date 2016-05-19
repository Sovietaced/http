package com.jasonparraga.triplebyte.http.handler;

import java.nio.file.Path;

public class HttpRequestHandlerUtils {

    /**
     * Resolves the file system path for the file specified by the given
     * resource path.
     * @param base
     * @param resourcePath
     * @return
     */
    public static Path getFileSystemPath(Path basePath, String resourcePath) {
        // Left strip leading dir
        resourcePath = resourcePath.replaceFirst("/", "");
        // Generate new path
        return basePath.resolve(resourcePath);
    }
}
