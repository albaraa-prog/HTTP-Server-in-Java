package HTTP.Protocol;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP request with method, URI, and headers.
 * This class is immutable and provides access to HTTP request components.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class HttpRequest {
    private final HttpMethod httpMethod;
    private final URI uri;
    private final Map<String, List<String>> requestHeaders;
    private final String body;

    /**
     * Private constructor for HttpRequest. Use the Builder pattern to create instances.
     * 
     * @param opCode the HTTP method (GET, POST, etc.)
     * @param uri the request URI
     * @param requestHeaders the HTTP headers as a map of header names to lists of values
     * @param body the request body
     */
    private HttpRequest(HttpMethod opCode,
                        URI uri,
                        Map<String, List<String>> requestHeaders,
                        String body) {
        this.httpMethod = opCode;
        this.uri = uri;
        this.requestHeaders = requestHeaders;
        this.body = body;
    }

    /**
     * Gets the URI of the HTTP request.
     * 
     * @return the request URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Gets the HTTP method of the request.
     * 
     * @return the HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Gets the HTTP headers of the request.
     * 
     * @return a map where keys are header names and values are lists of header values
     */
    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Gets the request body.
     * 
     * @return the request body as a string, or null if no body
     */
    public String getBody() {
        return body;
    }

    /**
     * Builder class for creating HttpRequest instances.
     * Implements the Builder pattern to provide a fluent API for constructing requests.
     * 
     * @author HTTP Server Team
     * @version 1.0
     */
    public static class Builder {
        private HttpMethod httpMethod;
        private URI uri;
        private Map<String, List<String>> requestHeaders;
        private String body;

        /**
         * Default constructor for Builder.
         */
        public Builder() {
        }

        /**
         * Sets the HTTP method for the request.
         * 
         * @param httpMethod the HTTP method to set
         * @return this Builder instance for method chaining
         */
        public Builder setHttpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        /**
         * Sets the URI for the request.
         * 
         * @param uri the URI to set
         * @return this Builder instance for method chaining
         */
        public Builder setUri(URI uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Sets the HTTP headers for the request.
         * 
         * @param requestHeaders the headers map to set
         * @return this Builder instance for method chaining
         */
        public Builder setRequestHeaders(Map<String, List<String>> requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }

        /**
         * Sets the request body.
         * 
         * @param body the request body to set
         * @return this Builder instance for method chaining
         */
        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        /**
         * Builds and returns a new HttpRequest instance.
         * 
         * @return a new HttpRequest with the configured values
         * @throws IllegalStateException if required fields are not set
         */
        public HttpRequest build() {
            // Validate required fields
            if (httpMethod == null) {
                throw new IllegalStateException("HTTP method must be set");
            }
            if (uri == null) {
                throw new IllegalStateException("URI must be set");
            }
            if (requestHeaders == null) {
                requestHeaders = Map.of(); // Use empty map as default
            }
            
            return new HttpRequest(httpMethod, uri, requestHeaders, body);
        }
    }
}