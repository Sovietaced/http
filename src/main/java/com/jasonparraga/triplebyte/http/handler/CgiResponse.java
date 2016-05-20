package com.jasonparraga.triplebyte.http.handler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.jasonparraga.triplebyte.http.HttpHeader;

public class CgiResponse {

    private final Map<HttpHeader, Set<String>> headers;
    private final byte[] body;

    private CgiResponse(Map<HttpHeader, Set<String>> headers, byte[] body) {
        this.headers = ImmutableMap.copyOf(headers);
        this.body = Arrays.copyOf(body, body.length);
    }

    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    public Map<HttpHeader, Set<String>> getHeaders() {
        return headers;
    }

    public static CgiResponse of(byte[] content) throws IOException {

        Map<HttpHeader, Set<String>> headers = new HashMap<>();

        ByteArrayInputStream is = new ByteArrayInputStream(content);

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);

        int contentLength = 0;

        // Read Headers
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            // Split header/values
            String[] headerValueSplit = line.split(":");
            HttpHeader header = HttpHeader.forValue(headerValueSplit[0]);
            String[] valuesSplit = headerValueSplit[1].split(",");
            Set<String> values = Sets.newHashSet(valuesSplit);
            headers.put(header, values);

            if (header == HttpHeader.CONTENT_LENGTH) {
                contentLength = Integer.parseInt(valuesSplit[0]);
            }

            // Move sentinel value
            line = reader.readLine();
        }

        // Read in content if available
        if (contentLength > 0) {
            int index = 0;
            byte[] responseContent = new byte[contentLength];

            while (index < contentLength && reader.ready()) {
                int valueRead = reader.read();
                responseContent[index] = (byte) valueRead;
                index++;
            }
            return new CgiResponse(headers, responseContent);
        } else {
            byte[] responseContent = CharStreams.toString(reader).getBytes();
            return new CgiResponse(headers, responseContent);
        }

    }
}
