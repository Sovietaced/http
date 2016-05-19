package com.jasonparraga.triplebyte.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * Java representation of an HTTP Request
 */
public class HttpRequest {

    private final Map<HttpHeader, Set<String>> headers;
    private final Verb verb;
    private final String path;
    private final String version;
    byte[] body;

    public enum Verb {
        GET,
        POST,
        PUT,
        PATCH,
        DELETE;
    }

    public HttpRequest(Builder b) {
        verb = b.verb;
        path = b.path;
        version = b.version;
        headers = ImmutableMap.copyOf(b.headers);
        body = b.body;
    }

    public static final class Builder {

        private Verb verb;
        private String path;
        private String version;
        private final Map<HttpHeader, Set<String>> headers = new HashMap<>();
        private byte[] body;

        public Builder() {
        }

        public Builder verb(Verb verb) {
            this.verb = verb;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder addHeader(HttpHeader header, Set<String> values) {
            this.headers.put(header, values);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = Arrays.copyOf(body, body.length);
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    /**
     * Reads an HttoRequest off the socket
     * @param socket
     * @return An HttpRequest if one was able to be read, otherwise empty.
     * @throws IOException
     */
    public static HttpRequest of(Socket socket) throws IOException {

        HttpRequest.Builder request = new HttpRequest.Builder();

        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        String requestLine = reader.readLine();

        // Read Request Line
        String[] requestLineParts = requestLine.split(" ");
        request.verb = Verb.valueOf(requestLineParts[0]);
        request.path = requestLineParts[1];
        request.version = requestLineParts[2];

        int contentLength = 0;

        // Read Headers
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            // Split header/values
            String[] headerValueSplit = line.split(": ");
            HttpHeader header = HttpHeader.forValue(headerValueSplit[0]);
            String[] valuesSplit = headerValueSplit[1].split(",");
            Set<String> values = Sets.newHashSet(valuesSplit);
            request.addHeader(header, values);

            if (header == HttpHeader.CONTENT_LENGTH) {
                contentLength = Integer.parseInt(valuesSplit[0]);
            }

            // Move sentinel value
            line = reader.readLine();
        }

        // Read in content if available
        if (contentLength > 0) {
            int index = 0;
            byte[] content = new byte[contentLength];

            while (index < contentLength && reader.ready()) {
                int valueRead = reader.read();
                content[index] = (byte) valueRead;
                index++;
            }
            request.body(content);
        }

        return request.build();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", verb, path, version);
    }

    public String getPath() {
        return path;
    }

    public Map<HttpHeader, Set<String>> getHeaders() {
        return headers;
    }

    public String getVersion() {
        return version;
    }

    public Verb getVerb() {
        return verb;
    }

    public byte[] getBody() {
        return body;
    }

}
