package com.jasonparraga.triplebyte.http.handler;

public enum CgiEnvironmentVariable {

    SERVER_PROTOCOL,
    SERVER_PORT,
    SERVER_NAME,
    REQUEST_METHOD;


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
