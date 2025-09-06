package HTTP.Request;

import HTTP.Protocol.HttpRequest;
import HTTP.Protocol.HttpResponse;

/**
 * Interface for handling HTTP requests and generating responses.
 * This interface is used for implementing route handlers in the HTTP server.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
@FunctionalInterface
public interface HttpRequestHandler {
    
    /**
     * Handles an HTTP request and returns an HTTP response.
     * 
     * @param request the HTTP request to handle
     * @return the HTTP response to send back
     */
    HttpResponse handle(HttpRequest request);
}
