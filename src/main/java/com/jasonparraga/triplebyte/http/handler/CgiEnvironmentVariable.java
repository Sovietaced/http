package com.jasonparraga.triplebyte.http.handler;

public enum CgiEnvironmentVariable {

    SERVER_PROTOCOL,
    SERVER_PORT,
    SERVER_NAME,
    REQUEST_METHOD,
    // Not used
    QUERY_STRING,
    REMOTE_ADDR,
    CONTENT_TYPE,
    CONTENT_LENGTH,
    HTTP_USER_AGENT;


    public static CgiEnvironmentVariable forValue(String value) {
        for(CgiEnvironmentVariable env : values()) {
            if(value.equals(env)) {
               return env;
            }
        }
        // If we don't find a environment variable throw an exception
        throw new IllegalArgumentException("No Environment Variable found for " + value);
    }
}
