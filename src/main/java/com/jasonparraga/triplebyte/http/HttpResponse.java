package com.jasonparraga.triplebyte.http;

import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.jasonparraga.triplebyte.http.handler.HttpRequestHandlerException;

public class HttpResponse {

    private final HttpStatus status;
    private final String path;
    private final String version;
    private final byte[] body;

    public HttpResponse(Builder b) {
        status = b.status;
        path = b.path;
        version = b.version;
        body = b.body;
    }

    public static final class Builder {

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

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }

    public String getPath() {
        return path;
    }

    public byte[] getBytes() {
        StringBuilder sb = new StringBuilder();

        // Append response line
        sb.append(String.format("%s %s \r\n", version, status));

        // Add content length header if we have content..
        if (body != null) {
            sb.append(String.format("%s: %d", HttpHeader.CONTENT_LENGTH.getValue(), body.length));
            sb.append("\r\n");
        }

        // End headers
        sb.append("\r\n");

        String headers = sb.toString();
        if (body != null) {
            return Bytes.concat(headers.getBytes(), body);
        } else {
            return headers.getBytes();
        }
    }

    public static HttpResponse notFound(HttpRequest request) {
        return new HttpResponse.Builder()
                .version(request.getVersion())
                .status(HttpStatus.notFound())
                .build();
    }

    public static HttpResponse ok(HttpRequest request, byte[] body) {
        return new HttpResponse.Builder()
                .version(request.getVersion())
                .status(HttpStatus.ok())
                .body(body)
                .build();
    }

    public static HttpResponse internalServerError(HttpRequest request, HttpRequestHandlerException e) {
        // TODO Auto-generated method stub
        return null;
    }

    public HttpStatus getStatus() {
        return status;
    }
}