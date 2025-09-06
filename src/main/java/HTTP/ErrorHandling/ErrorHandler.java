package HTTP.ErrorHandling;

import java.util.HashMap;
import java.util.Map;

import HTTP.Protocol.HttpResponse;

/**
 * Comprehensive error handling system for HTTP responses.
 * Provides standardized error responses with appropriate status codes
 * and user-friendly error messages.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class ErrorHandler {
    
    private static final Map<Integer, String> STATUS_MESSAGES = new HashMap<>();
    
    static {
        // Client errors (4xx)
        STATUS_MESSAGES.put(400, "Bad Request");
        STATUS_MESSAGES.put(401, "Unauthorized");
        STATUS_MESSAGES.put(403, "Forbidden");
        STATUS_MESSAGES.put(404, "Not Found");
        STATUS_MESSAGES.put(405, "Method Not Allowed");
        STATUS_MESSAGES.put(406, "Not Acceptable");
        STATUS_MESSAGES.put(408, "Request Timeout");
        STATUS_MESSAGES.put(409, "Conflict");
        STATUS_MESSAGES.put(413, "Payload Too Large");
        STATUS_MESSAGES.put(415, "Unsupported Media Type");
        STATUS_MESSAGES.put(429, "Too Many Requests");
        
        // Server errors (5xx)
        STATUS_MESSAGES.put(500, "Internal Server Error");
        STATUS_MESSAGES.put(501, "Not Implemented");
        STATUS_MESSAGES.put(502, "Bad Gateway");
        STATUS_MESSAGES.put(503, "Service Unavailable");
        STATUS_MESSAGES.put(504, "Gateway Timeout");
        STATUS_MESSAGES.put(505, "HTTP Version Not Supported");
    }
    
    /**
     * Creates a standardized error response.
     * 
     * @param statusCode the HTTP status code
     * @param message optional custom error message
     * @return HttpResponse with error details
     */
    public static HttpResponse createErrorResponse(int statusCode, String message) {
        String statusMessage = STATUS_MESSAGES.getOrDefault(statusCode, "Unknown Error");
        String errorMessage = message != null ? message : statusMessage;
        
        // Create HTML error page
        String htmlContent = createErrorHtml(statusCode, statusMessage, errorMessage);
        
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("text/html; charset=utf-8"));
        headers.put("Content-Length", java.util.List.of(String.valueOf(htmlContent.length())));
        
        return new HttpResponse(statusCode, headers, htmlContent);
    }
    
    /**
     * Creates a standardized error response with default message.
     * 
     * @param statusCode the HTTP status code
     * @return HttpResponse with error details
     */
    public static HttpResponse createErrorResponse(int statusCode) {
        return createErrorResponse(statusCode, null);
    }
    
    /**
     * Creates a 404 Not Found error response.
     * 
     * @param requestedUri the URI that was not found
     * @return HttpResponse with 404 error
     */
    public static HttpResponse createNotFoundResponse(String requestedUri) {
        String message = "The requested resource '" + requestedUri + "' was not found on this server.";
        return createErrorResponse(404, message);
    }
    
    /**
     * Creates a 405 Method Not Allowed error response.
     * 
     * @param method the HTTP method that was not allowed
     * @param allowedMethods the allowed HTTP methods
     * @return HttpResponse with 405 error
     */
    public static HttpResponse createMethodNotAllowedResponse(String method, String... allowedMethods) {
        String message = "HTTP method '" + method + "' is not allowed for this resource.";
        HttpResponse response = createErrorResponse(405, message);
        
        // Add Allow header
        if (allowedMethods.length > 0) {
            String allowHeader = String.join(", ", allowedMethods);
            response.getResponseHeaders().put("Allow", java.util.List.of(allowHeader));
        }
        
        return response;
    }
    
    /**
     * Creates a 500 Internal Server Error response.
     * 
     * @param exception the exception that caused the error
     * @return HttpResponse with 500 error
     */
    public static HttpResponse createInternalServerErrorResponse(Exception exception) {
        String message = "An internal server error occurred: " + exception.getMessage();
        return createErrorResponse(500, message);
    }
    
    /**
     * Creates a 400 Bad Request error response.
     * 
     * @param reason the reason for the bad request
     * @return HttpResponse with 400 error
     */
    public static HttpResponse createBadRequestResponse(String reason) {
        String message = "Bad Request: " + reason;
        return createErrorResponse(400, message);
    }
    
    /**
     * Creates a 413 Payload Too Large error response.
     * 
     * @param maxSize the maximum allowed payload size
     * @return HttpResponse with 413 error
     */
    public static HttpResponse createPayloadTooLargeResponse(long maxSize) {
        String message = "Request payload exceeds maximum allowed size of " + maxSize + " bytes.";
        return createErrorResponse(413, message);
    }
    
    /**
     * Creates an HTML error page.
     * 
     * @param statusCode the HTTP status code
     * @param statusMessage the status message
     * @param errorMessage the detailed error message
     * @return HTML content for the error page
     */
    private static String createErrorHtml(int statusCode, String statusMessage, String errorMessage) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%d %s</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #667eea;
                        margin: 0;
                        padding: 0;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .error-container {
                        background: white;
                        border-radius: 12px;
                        padding: 40px;
                        box-shadow: 0 20px 40px #000000;
                        text-align: center;
                        max-width: 500px;
                        margin: 20px;
                    }
                    .error-code {
                        font-size: 72px;
                        font-weight: bold;
                        color: #e74c3c;
                        margin: 0;
                        line-height: 1;
                    }
                    .error-title {
                        font-size: 24px;
                        color: #2c3e50;
                        margin: 20px 0;
                        font-weight: 600;
                    }
                    .error-message {
                        color: #7f8c8d;
                        line-height: 1.6;
                        margin-bottom: 30px;
                    }
                    .home-link {
                        display: inline-block;
                        background: #3498db;
                        color: white;
                        padding: 12px 24px;
                        text-decoration: none;
                        border-radius: 6px;
                        transition: background 0.3s;
                    }
                    .home-link:hover {
                        background: #2980b9;
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <h1 class="error-code">%d</h1>
                    <h2 class="error-title">%s</h2>
                    <p class="error-message">%s</p>
                    <a href="/" class="home-link">Go Home</a>
                </div>
            </body>
            </html>
            """, statusCode, statusMessage, statusCode, statusMessage, errorMessage);
    }
    
    /**
     * Gets the status message for a given status code.
     * 
     * @param statusCode the HTTP status code
     * @return the status message
     */
    public static String getStatusMessage(int statusCode) {
        return STATUS_MESSAGES.getOrDefault(statusCode, "Unknown Status");
    }
    
    /**
     * Checks if a status code represents an error.
     * 
     * @param statusCode the HTTP status code
     * @return true if the status code represents an error
     */
    public static boolean isError(int statusCode) {
        return statusCode >= 400;
    }
    
    /**
     * Checks if a status code represents a client error.
     * 
     * @param statusCode the HTTP status code
     * @return true if the status code represents a client error
     */
    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * Checks if a status code represents a server error.
     * 
     * @param statusCode the HTTP status code
     * @return true if the status code represents a server error
     */
    public static boolean isServerError(int statusCode) {
        return statusCode >= 500;
    }
}
