package com.jasonparraga.triplebyte.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
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

    public enum Verb {
        GET,
        POST;
    }

    public HttpRequest(Builder b) {
        verb = b.verb;
        path = b.path;
        version = b.version;
        headers = ImmutableMap.copyOf(b.headers);
    }

    public static final class Builder {

        private Verb verb;
        private String path;
        private String version;
        private final Map<HttpHeader, Set<String>> headers = new HashMap<>();

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

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    /**
     * Parses an {@link HttpRequest} from a list of strings.
     * @param input
     * @return
     */
    public static HttpRequest of(List<String> input) {
        HttpRequest.Builder request = new HttpRequest.Builder();
        System.out.println(input);
        // Parse the request line
        String requestLine = input.get(0);
        String[] requestLineParts = requestLine.split(" ");
        request.verb = Verb.valueOf(requestLineParts[0]);
        request.path = requestLineParts[1];
        request.version = requestLineParts[2];

        return request.build();
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

        // Read Headers
        String line = reader.readLine();
        while (line != null) {
            if (line.isEmpty()) {
                // Done reading headers
                break;
            }

            // Split header/values
            String[] headerValueSplit = line.split(": ");
            HttpHeader header = HttpHeader.forValue(headerValueSplit[0]);
            String[] valuesSplit = headerValueSplit[1].split(",");
            Set<String> values = Sets.newHashSet(valuesSplit);
            request.addHeader(header, values);

            // Move sentinel value
            line = reader.readLine();
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

}
