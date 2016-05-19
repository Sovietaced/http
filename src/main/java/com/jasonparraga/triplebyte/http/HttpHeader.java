package com.jasonparraga.triplebyte.http;

public enum HttpHeader {

    HOST("Host"),
    USER_AGENT("User-Agent"),
    ACCEPT("Accept"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_ENCODING("Accept-Encoding"),
    CONNECTION("Connection"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type");

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    public static HttpHeader forValue(String value) {
        for(HttpHeader h : values()) {
            if(value.equals(h.getValue())) {
               return h;
            }
        }
        // If we don't find a header throw an exception
        throw new IllegalArgumentException("No Header found for " + value);
    }
}
