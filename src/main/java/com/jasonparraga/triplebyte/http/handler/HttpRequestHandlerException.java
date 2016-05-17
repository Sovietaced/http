package com.jasonparraga.triplebyte.http.handler;

/**
 * Thrown for exceptional cases when the server cannot handle a request
 */
public class HttpRequestHandlerException extends Exception {

    private static final long serialVersionUID = 1L;

    public HttpRequestHandlerException(){
        this("Unspecified Http Request Handler Exception");
   }

   public HttpRequestHandlerException(String message){
       super(message);
   }

   public HttpRequestHandlerException(String message, Throwable cause){
       super(message, cause);
   }

   public HttpRequestHandlerException(Throwable cause){
       super(cause);
   }
}
