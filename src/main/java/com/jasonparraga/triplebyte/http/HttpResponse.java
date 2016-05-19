package com.jasonparraga.triplebyte.http;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;

/**
 * Java representation of an HTTP response
 */
public class HttpResponse {

    private final Map<HttpHeader, Set<String>> headers;
    private final HttpStatus status;
    private final String path;
    private final String version;
    private final byte[] body;

    public HttpResponse(Builder b) {
        headers = ImmutableMap.copyOf(b.headers);
        status = b.status;
        path = b.path;
        version = b.version;
        body = b.body;
    }

    public static final class Builder {

        private final Map<HttpHeader, Set<String>> headers = new HashMap<>();
        private HttpStatus status;
        private String path;
        private String version;
        private byte[] body;

        public Builder() {
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = Arrays.copyOf(body, body.length);
            return this;
        }

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder addHeader(HttpHeader header, Set<String> values) {
            this.headers.put(header, values);
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }

    public String getPath() {
        return path;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public byte[] getBytes() {
        StringBuilder sb = new StringBuilder();

        // Append response line
        sb.append(String.format("%s %s \r\n", version, status));

        // Add headers
        for (Entry<HttpHeader, Set<String>> entry : headers.entrySet()) {
            HttpHeader header = entry.getKey();
            String value = Joiner.on(", ").join(entry.getValue());
            sb.append(String.format("%s: %s", header.getValue(), value));
            sb.append("\r\n");
        }

        // Add content length header if we have content..
        if (body != null) {
            sb.append(String.format("%s: %d", HttpHeader.CONTENT_LENGTH.getValue(), body.length));
            sb.append("\r\n");
        }

        // End headers
        sb.append("\r\n");

        String headerString = sb.toString();
        if (body != null) {
            return Bytes.concat(headerString.getBytes(), body);
        } else {
            return headerString.getBytes();
        }
    }

    public static HttpResponse notFound(HttpRequest request) {
        return new HttpResponse.Builder()
                .version(request.getVersion())
                .status(HttpStatus.notFound())
                .addHeader(HttpHeader.CONTENT_TYPE, Collections.singleton("text/plain"))
                .body("Not Found\n".getBytes())
                .build();
    }

    public static HttpResponse ok(HttpRequest request, byte[] body) {
        return new HttpResponse.Builder()
                .version(request.getVersion())
                .status(HttpStatus.ok())
                .body(body)
                .build();
    }

    public static HttpResponse internalServerError(HttpRequest request) {
        return new HttpResponse.Builder()
                .version(request.getVersion())
                .status(HttpStatus.internalServerError())
                .addHeader(HttpHeader.CONTENT_TYPE, Collections.singleton("text/plain"))
                .body("Internal Sever Error\n".getBytes())
                .build();
    }

    public static HttpResponse requestTimedOut(HttpRequest request) {
        return new HttpResponse.Builder()
                .version(request.getVersion())
                .status(HttpStatus.requestTimedOut())
                .addHeader(HttpHeader.CONTENT_TYPE, Collections.singleton("text/plain"))
                .body("Request Timed Out\n".getBytes())
                .build();
    }
}
