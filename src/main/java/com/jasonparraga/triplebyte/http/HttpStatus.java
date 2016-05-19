package com.jasonparraga.triplebyte.http;

public class HttpStatus {

    private final String value;
    private final int code;

    private HttpStatus(String value, int code) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + " " + value;
    }
    public static HttpStatus ok() {
        return new HttpStatus("OK", 200);
    }

    public static HttpStatus notFound() {
        return new HttpStatus("NOT FOUND", 404);
    }

    public static HttpStatus internalServerError() {
        return new HttpStatus("INTERNAL SERVER ERROR", 500);
    }

    public static HttpStatus requestTimedOut() {
        return new HttpStatus("REQUEST TIMED OUT", 408);
    }
}
