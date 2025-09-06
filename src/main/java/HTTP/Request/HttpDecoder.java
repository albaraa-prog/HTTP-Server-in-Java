package HTTP.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import HTTP.Protocol.HttpMethod;
import HTTP.Protocol.HttpRequest;

/**
 * HTTP request decoder that parses raw HTTP requests from input streams.
 * This class handles the parsing of HTTP request lines, headers, and constructs
 * HttpRequest objects for further processing.
 * 
 * <p>The decoder supports standard HTTP/1.1 request format:
 * <ul>
 *   <li>Request line: METHOD URI HTTP_VERSION</li>
 *   <li>Headers: Name: Value format</li>
 *   <li>Multiple values for the same header name</li>
 * </ul>
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class HttpDecoder {

    /**
     * Decodes an HTTP request from an input stream.
     * 
     * <p>This method reads and parses the complete HTTP request including:
     * <ul>
     *   <li>Request line (method, URI, HTTP version)</li>
     *   <li>HTTP headers</li>
     *   <li>Request body (if present)</li>
     * </ul>
     * 
     * <p>The method returns an Optional containing the parsed HttpRequest if successful,
     * or an empty Optional if the request cannot be parsed or an error occurs.
     * 
     * @param inputStream the input stream containing the raw HTTP request
     * @return an Optional containing the parsed HttpRequest, or empty if parsing fails
     */
    public static Optional<HttpRequest> decode(final InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            // Read the request line
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                return Optional.empty();
            }
            
            // Parse request line: METHOD URI HTTP_VERSION
            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                return Optional.empty();
            }
            
            String method = parts[0];
            String uri = parts[1];
            // String version = parts[2]; // HTTP version not used in current implementation
            
            // Parse HTTP method
            HttpMethod httpMethod = parseHttpMethod(method);
            if (httpMethod == null) {
                return Optional.empty();
            }
            
            // Parse URI
            URI requestUri = parseUri(uri);
            if (requestUri == null) {
                return Optional.empty();
            }
            
            // Read headers
            Map<String, List<String>> headers = readHeaders(reader);
            
            // Read body if present
            String body = readBody(reader, headers);
            
            // Create HttpRequest
            HttpRequest request = new HttpRequest.Builder()
                    .setHttpMethod(httpMethod)
                    .setUri(requestUri)
                    .setRequestHeaders(headers)
                    .setBody(body)
                    .build();
            
            return Optional.of(request);
            
        } catch (IOException e) {
            System.err.println("Error reading HTTP request: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Parses an HTTP method string into an HttpMethod enum value.
     * 
     * <p>This method converts the method string to uppercase and attempts to
     * match it against the HttpMethod enum values. Common HTTP methods include
     * GET, POST, PUT, DELETE, HEAD, OPTIONS, etc.
     * 
     * @param method the HTTP method string to parse
     * @return the corresponding HttpMethod enum value, or null if parsing fails
     */
    private static HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Parses a URI string into a URI object.
     * 
     * <p>This method validates the URI syntax and creates a URI object.
     * It handles both absolute and relative URIs as per RFC 3986.
     * 
     * @param uri the URI string to parse
     * @return the parsed URI object, or null if the URI is malformed
     */
    private static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    /**
     * Reads and parses HTTP headers from a BufferedReader.
     * 
     * <p>This method reads header lines until it encounters an empty line,
     * which indicates the end of headers. Each header line follows the format:
     * <code>HeaderName: HeaderValue</code>
     * 
     * <p>The method supports multiple values for the same header name by
     * storing them in a List. Headers are case-insensitive according to
     * HTTP specifications.
     * 
     * @param reader the BufferedReader to read headers from
     * @return a Map containing header names mapped to lists of header values
     * @throws IOException if an I/O error occurs while reading
     */
    private static Map<String, List<String>> readHeaders(BufferedReader reader) throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        String line;
        
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            // Parse header line: Name: Value
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                
                headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }
        }
        
        return headers;
    }
    
    /**
     * Reads the request body if present.
     * 
     * <p>This method checks for a Content-Length header and reads the specified
     * number of characters from the input stream. If no Content-Length is present,
     * no body is read.
     * 
     * @param reader the BufferedReader to read from
     * @param headers the request headers to check for Content-Length
     * @return the request body as a string, or null if no body
     * @throws IOException if an I/O error occurs while reading
     */
    private static String readBody(BufferedReader reader, Map<String, List<String>> headers) throws IOException {
        // Check for Content-Length header
        List<String> contentLengthValues = headers.get("Content-Length");
        if (contentLengthValues == null || contentLengthValues.isEmpty()) {
            return null;
        }
        
        try {
            int contentLength = Integer.parseInt(contentLengthValues.get(0));
            if (contentLength <= 0) {
                return null;
            }
            
            // Read the specified number of characters
            char[] bodyChars = new char[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int charsRead = reader.read(bodyChars, totalRead, contentLength - totalRead);
                if (charsRead == -1) {
                    break; // End of stream
                }
                totalRead += charsRead;
            }
            
            return new String(bodyChars, 0, totalRead);
            
        } catch (NumberFormatException e) {
            // Invalid Content-Length header
            return null;
        }
    }
}