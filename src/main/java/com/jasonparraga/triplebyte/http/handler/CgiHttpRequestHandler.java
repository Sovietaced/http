package com.jasonparraga.triplebyte.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.jasonparraga.triplebyte.http.HttpHeader;
import com.jasonparraga.triplebyte.http.HttpRequest;
import com.jasonparraga.triplebyte.http.HttpResponse;
import com.jasonparraga.triplebyte.http.HttpStatus;

public class CgiHttpRequestHandler implements HttpRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(CgiHttpRequestHandler.class);

    private final Path fileSystemBasePath;
    private final ExecutorService execService = Executors.newFixedThreadPool(1);
    private int time = DEFAULT_TIME;
    private TimeUnit timeUnit = DEFAULT_TIME_UNIT;

    private static final String CGI_DIR = "/cgi-bin";
    private static final int DEFAULT_TIME = 10;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    public CgiHttpRequestHandler(Path fileSystemBasePath) {
        this.fileSystemBasePath = fileSystemBasePath;
    }

    public CgiHttpRequestHandler(Path fileSystemBasePath, int time, TimeUnit timeUnit) {
        this.fileSystemBasePath = fileSystemBasePath;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    @Override
    public Optional<HttpResponse> handleRequest(HttpRequest request) throws HttpRequestHandlerException {

        if (request.getPath().startsWith(CGI_DIR)) {
            return Optional.of(handleCgiRequest(request));
        } else {
            return Optional.empty();
        }
    }

    public HttpResponse handleCgiRequest(HttpRequest request) throws HttpRequestHandlerException {
        Path fileSystemPath = getFileSystemPath(request.getPath());

        CgiTask task = new CgiTask(fileSystemPath, request);
        Future<byte[]> future = execService.submit(task);

        try {
            byte[] result = future.get(time, timeUnit);

            CgiResponse cgiResponse = CgiResponse.of(result);

            HttpResponse.Builder builder = new HttpResponse.Builder()
                    .version(request.getVersion())
                    .addHeaders(cgiResponse.getHeaders())
                    .status(HttpStatus.ok())
                    .body(cgiResponse.getBody());

            return builder.build();
        } catch (TimeoutException e) {
            // Handle timeouts!
            log.warn("Timed out handling request {}", request, e);
            return HttpResponse.requestTimedOut(request);
        } catch (InterruptedException | ExecutionException | IOException e) {
            // Anything else should be an internal server error
            throw new HttpRequestHandlerException(e);
        }

    }

    private Path getFileSystemPath(String resourcePath) {
        // Left strip leading dir and cgi-bin
        resourcePath = resourcePath.replaceFirst(CGI_DIR, "");
        resourcePath = resourcePath.replaceFirst("/", "");
        // Generate new path
        return fileSystemBasePath.resolve(resourcePath);
    }

    public class CgiTask implements Callable<byte[]> {

        private final Path path;
        private final HttpRequest request;

        public CgiTask(Path path, HttpRequest request) {
            this.path = path;
            this.request = request;
        }

        @Override
        public byte[] call() throws IOException, InterruptedException {
            List<String> command = new ArrayList<String>();
            // The command is simply the file path to execute
            command.add(path.toAbsolutePath().toString());

            log.info("Executing process with command {}.", command);

            ProcessBuilder builder = new ProcessBuilder(command);

            loadEnvironmentVariables(builder, request);

            long start = System.nanoTime();
            final Process process = builder.start();

            // If we have any POST data write it to STD IN
            if (request.getBody() != null) {
                process.getOutputStream().write(request.getBody());
                process.getOutputStream().flush();
                process.getOutputStream().close();
            }
            // Wait for the process to complete
            process.waitFor();

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.info("Process with command {} exited with code {} in {} ms",
                     command, process.exitValue(), elapsedMs);

            // Read in STD out
            InputStream is = process.getInputStream();
            return ByteStreams.toByteArray(is);
        }
    }

    private Map<String, String> loadEnvironmentVariables(ProcessBuilder builder, HttpRequest request) {
        Map<String, String> environ = builder.environment();

        for (Entry<HttpHeader, Set<String>> entry : request.getHeaders().entrySet()) {
            HttpHeader header = entry.getKey();
            String value = Joiner.on(",").join(entry.getValue());

            if (header == HttpHeader.CONTENT_TYPE) {
                environ.put(CgiEnvironmentVariable.CONTENT_TYPE.toString(), value);
            } else if (header == HttpHeader.HOST) {
                environ.put(CgiEnvironmentVariable.SERVER_NAME.toString(), value);
            } else if (header == HttpHeader.CONTENT_LENGTH) {
                environ.put(CgiEnvironmentVariable.CONTENT_LENGTH.toString(), value);
            } else if (header == HttpHeader.USER_AGENT) {
                environ.put(CgiEnvironmentVariable.HTTP_USER_AGENT.toString(), value);
            }
        }

        // Handle basic headers form request line
        environ.put(CgiEnvironmentVariable.REQUEST_METHOD.toString(), request.getVerb().toString());
        environ.put(CgiEnvironmentVariable.SERVER_PROTOCOL.toString(), request.getVersion());

        return environ;

    }

}
