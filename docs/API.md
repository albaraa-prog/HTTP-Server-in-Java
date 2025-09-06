# HTTP Server API Documentation

## 📚 Overview

This document provides comprehensive API documentation for all classes, methods, and interfaces in the HTTP Server project. Each component is documented with its purpose, methods, parameters, and usage examples.

---

## 🏗️ Core Server Classes

### **Server Class**

**Package**: `HTTP`  
**Purpose**: Main server class that manages connections, routing, and request handling.

#### **Constructors**

```java
public Server(int port) throws IOException
public Server() throws IOException  // Default port 8080
```

#### **Public Methods**

```java
public void start()                    // Starts the HTTP server
public void stop()                     // Stops the HTTP server gracefully
public ServerConfig getConfig()        // Returns server configuration
public ServerLogger getLogger()        // Returns server logger instance
```

#### **Private Methods**

```java
private void initializeDefaultRoutes()           // Sets up default route handlers
private HttpResponse serveStaticFile(String path) // Serves static files
private HttpResponse createMetricsResponse()      // Creates metrics endpoint response
private HttpResponse createConfigResponse()       // Creates config endpoint response
private HttpResponse createFileListingResponse()  // Creates file listing response
private void handleConnection(Socket clientSocket) // Handles client connections
private void writeResponse(Socket clientSocket, HttpResponse response) // Writes HTTP responses
```

#### **Usage Example**

```java
try {
    Server server = new Server(8080);
    server.start();
} catch (IOException e) {
    System.err.println("Failed to start server: " + e.getMessage());
}
```

---

### **Main Class**

**Package**: `HTTP`  
**Purpose**: Entry point for the HTTP server application.

#### **Public Methods**

```java
public static void main(String[] args)  // Main method - server entry point
```

#### **Usage Example**

```bash
# Run with default port (8080)
mvn exec:java -Dexec.mainClass="HTTP.Main"

# Run with custom port
mvn exec:java -Dexec.mainClass="HTTP.Main" -Dexec.args="9000"
```

---

## 🔧 Configuration & Management

### **ServerConfig Class**

**Package**: `HTTP`  
**Purpose**: Manages server configuration including loading from files and environment variables.

#### **Constants**

```java
private static final int DEFAULT_PORT = 8080
private static final String DEFAULT_STATIC_DIR = "public"
private static final int DEFAULT_THREAD_POOL_SIZE = 100
private static final String DEFAULT_LOG_LEVEL = "INFO"
private static final boolean DEFAULT_ENABLE_LOGGING = true
private static final boolean DEFAULT_ENABLE_MONITORING = true
```

#### **Public Methods**

```java
public ServerConfig()                                    // Default constructor
public ServerConfig(int port)                            // Constructor with custom port
public ServerConfig loadFromFile(String configFile)      // Loads config from properties file
public ServerConfig loadFromEnvironment()                 // Loads config from environment variables
public String getProperty(String key)                    // Gets config property value
public String getProperty(String key, String defaultValue) // Gets config property with default
```

#### **Getters**

```java
public int getPort()                    // Returns configured port
public String getStaticDirectory()       // Returns static file directory
public int getThreadPoolSize()          // Returns thread pool size
public Level getLogLevel()              // Returns logging level
public boolean isLoggingEnabled()       // Returns if logging is enabled
public boolean isMonitoringEnabled()    // Returns if monitoring is enabled
```

#### **Usage Example**

```java
ServerConfig config = new ServerConfig()
    .loadFromFile("config.properties")
    .loadFromEnvironment();

int port = config.getPort();
String staticDir = config.getStaticDirectory();
```

---

## 📡 HTTP Protocol Classes

### **HttpRequest Class**

**Package**: `HTTP`  
**Purpose**: Represents an HTTP request with method, URI, and headers.

#### **Private Fields**

```java
private final HttpMethod httpMethod
private final URI uri
private final Map<String, List<String>> requestHeaders
```

#### **Public Methods**

```java
public URI getUri()                     // Returns request URI
public HttpMethod getHttpMethod()       // Returns HTTP method
public Map<String, List<String>> getRequestHeaders() // Returns request headers
```

#### **Builder Class**

```java
public static class Builder {
    public Builder()                                    // Default constructor
    public Builder setHttpMethod(HttpMethod method)     // Sets HTTP method
    public Builder setUri(URI uri)                      // Sets request URI
    public Builder setRequestHeaders(Map<String, List<String>> headers) // Sets headers
    public HttpRequest build()                          // Builds HttpRequest instance
}
```

#### **Usage Example**

```java
HttpRequest request = new HttpRequest.Builder()
    .setHttpMethod(HttpMethod.GET)
    .setUri(new URI("/api/users"))
    .setRequestHeaders(headers)
    .build();
```

---

### **HttpResponse Class**

**Package**: `HTTP`  
**Purpose**: Represents an HTTP response with status code, headers, and body.

#### **Private Fields**

```java
private final int statusCode
private final Map<String, List<String>> responseHeaders
private final Object entity
```

#### **Public Methods**

```java
public HttpResponse(int statusCode, Map<String, List<String>> headers, Object entity)
public int getStatusCode()                              // Returns HTTP status code
public Map<String, List<String>> getResponseHeaders()  // Returns response headers
public Optional<Object> getEntity()                    // Returns response body
```

#### **Usage Example**

```java
Map<String, List<String>> headers = new HashMap<>();
headers.put("Content-Type", List.of("text/html"));
headers.put("Content-Length", List.of("123"));

HttpResponse response = new HttpResponse(200, headers, "<html>...</html>");
```

---

### **HttpMethod Enum**

**Package**: `HTTP`  
**Purpose**: Defines supported HTTP methods.

#### **Values**

```java
GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE
```

#### **Usage Example**

```java
if (request.getHttpMethod() == HttpMethod.GET) {
    // Handle GET request
}
```

---

### **HttpStatusCode Enum**

**Package**: `HTTP`  
**Purpose**: Defines HTTP status codes and their descriptions.

#### **Common Values**

```java
OK(200, "OK")
CREATED(201, "Created")
NO_CONTENT(204, "No Content")
BAD_REQUEST(400, "Bad Request")
NOT_FOUND(404, "Not Found")
INTERNAL_SERVER_ERROR(500, "Internal Server Error")
```

#### **Public Methods**

```java
public int getCode()                    // Returns numeric status code
public String getDescription()          // Returns status description
```

---

## 🔄 Request Processing

### **HttpDecoder Class**

**Package**: `HTTP`  
**Purpose**: Decodes raw HTTP requests from input streams.

#### **Public Methods**

```java
public static Optional<HttpRequest> decode(InputStream inputStream) // Decodes HTTP request
```

#### **Private Methods**

```java
private static HttpMethod parseHttpMethod(String method)           // Parses HTTP method
private static URI parseUri(String uri)                           // Parses request URI
private static Map<String, List<String>> readHeaders(BufferedReader reader) // Reads headers
```

#### **Usage Example**

```java
Optional<HttpRequest> requestOpt = HttpDecoder.decode(clientSocket.getInputStream());
if (requestOpt.isPresent()) {
    HttpRequest request = requestOpt.get();
    // Process request
}
```

---

### **HttpRequestHandler Interface**

**Package**: `HTTP`  
**Purpose**: Functional interface for handling HTTP requests.

#### **Methods**

```java
@FunctionalInterface
public interface HttpRequestHandler {
    HttpResponse handle(HttpRequest request);  // Handles HTTP request and returns response
}
```

#### **Usage Example**

```java
HttpRequestHandler handler = (request) -> {
    // Custom request handling logic
    return new HttpResponse(200, headers, "Response content");
};

routes.put("GET/custom", handler);
```

---

### **Routes Class**

**Package**: `HTTP`  
**Purpose**: Manages route registration and lookup.

#### **Private Fields**

```java
private final Map<String, RequestRunner> routes
```

#### **Public Methods**

```java
public Routes()                                        // Constructor
public void addRoute(HttpMethod opCode, String route, RequestRunner runner) // Adds route
public Map<String, RequestRunner> getRoutes()          // Returns all routes
```

---

## 📁 Static File Serving

### **StaticFileHandler Class**

**Package**: `HTTP`  
**Purpose**: Handles serving static files with MIME type detection and security.

#### **Private Fields**

```java
private final String staticDirectory
private final Map<String, String> mimeTypes
```

#### **Public Methods**

```java
public StaticFileHandler(String staticDirectory)       // Constructor
public boolean canServe(String uri)                    // Checks if file can be served
public HttpResponse serveFile(String uri)              // Serves file and returns response
public String getStaticDirectory()                     // Returns static directory path
public String[] listFiles()                           // Lists available files
```

#### **Private Methods**

```java
private String getMimeType(String uri)                 // Determines MIME type
private String getFileExtension(String uri)            // Extracts file extension
private HttpResponse createErrorResponse(int statusCode, String message) // Creates error response
private Map<String, String> initializeMimeTypes()     // Initializes MIME type mapping
```

#### **Supported MIME Types**

- **Text**: HTML, CSS, JavaScript, JSON, XML, Plain text
- **Images**: PNG, JPEG, GIF, SVG, ICO, WebP
- **Documents**: PDF, DOC, DOCX
- **Archives**: ZIP, TAR, GZ

#### **Usage Example**

```java
StaticFileHandler handler = new StaticFileHandler("public");
if (handler.canServe("index.html")) {
    HttpResponse response = handler.serveFile("index.html");
    // Send response to client
}
```

---

## ❌ Error Handling

### **ErrorHandler Class**

**Package**: `HTTP`  
**Purpose**: Provides comprehensive error handling with standardized error responses.

#### **Static Fields**

```java
private static final Map<Integer, String> STATUS_MESSAGES
```

#### **Public Methods**

```java
public static HttpResponse createErrorResponse(int statusCode)           // Creates error response
public static HttpResponse createErrorResponse(int statusCode, String message) // Creates custom error
public static HttpResponse createNotFoundResponse(String requestedUri)   // Creates 404 response
public static HttpResponse createMethodNotAllowedResponse(String method, String... allowedMethods) // Creates 405 response
public static HttpResponse createInternalServerErrorResponse(Exception exception) // Creates 500 response
public static HttpResponse createBadRequestResponse(String reason)      // Creates 400 response
public static HttpResponse createPayloadTooLargeResponse(long maxSize)  // Creates 413 response
public static String getStatusMessage(int statusCode)                   // Gets status message
public static boolean isError(int statusCode)                          // Checks if status is error
public static boolean isClientError(int statusCode)                    // Checks if client error
public static boolean isServerError(int statusCode)                    // Checks if server error
```

#### **Private Methods**

```java
private static String createErrorHtml(int statusCode, String statusMessage, String errorMessage) // Creates HTML error page
```

#### **Usage Example**

```java
// Create 404 error response
HttpResponse errorResponse = ErrorHandler.createNotFoundResponse("/missing-page");

// Create custom error response
HttpResponse customError = ErrorHandler.createErrorResponse(400, "Invalid request format");

// Check if status code represents an error
if (ErrorHandler.isError(404)) {
    // Handle error status
}
```

---

## 📊 Logging & Monitoring

### **ServerLogger Class**

**Package**: `HTTP`  
**Purpose**: Provides structured logging and performance monitoring.

#### **Private Fields**

```java
private static final Logger logger
private static final DateTimeFormatter TIMESTAMP_FORMAT
private final AtomicLong totalRequests
private final AtomicLong successfulRequests
private final AtomicLong failedRequests
private final AtomicLong totalResponseTime
private final ConcurrentHashMap<String, AtomicLong> endpointRequests
private final ConcurrentHashMap<Integer, AtomicLong> statusCodeCounts
private final boolean enableLogging
private final boolean enableMonitoring
```

#### **Public Methods**

```java
public ServerLogger(boolean enableLogging, boolean enableMonitoring)   // Constructor
public void logServerStart(int port)                                   // Logs server startup
public void logServerStop()                                            // Logs server shutdown
public void logConnection(Socket clientSocket)                         // Logs new connection
public void logRequest(String method, String uri, int statusCode, long responseTime) // Logs request
public void logError(String message, Throwable throwable)              // Logs error
public void logWarning(String message)                                 // Logs warning
public void logInfo(String message)                                    // Logs info message
public String getMetrics()                                             // Returns performance metrics
public void resetMetrics()                                             // Resets all metrics
```

#### **Private Methods**

```java
private void updateMetrics(String method, String uri, int statusCode, long responseTime) // Updates metrics
private String getCurrentTimestamp()                                   // Gets formatted timestamp
```

#### **Usage Example**

```java
ServerLogger logger = new ServerLogger(true, true);

// Log server events
logger.logServerStart(8080);
logger.logConnection(clientSocket);
logger.logRequest("GET", "/", 200, 15);

// Get performance metrics
String metrics = logger.getMetrics();
System.out.println(metrics);
```

---

## 🛠️ Utility Classes

### **buildHeaderStrings Class**

**Package**: `HTTP`  
**Purpose**: Utility for building HTTP header strings.

#### **Public Methods**

```java
public static String buildHeaderStrings(Map<String, List<String>> headers) // Builds header string
```

---

### **getResponseString Class**

**Package**: `HTTP`  
**Purpose**: Utility for generating HTTP response strings.

#### **Public Methods**

```java
public static String getResponseString(HttpResponse response) // Generates response string
```

---

### **writeResponse Class**

**Package**: `HTTP`  
**Purpose**: Utility for writing HTTP responses to output streams.

#### **Public Methods**

```java
public static void writeResponse(OutputStream output, HttpResponse response) // Writes response
```

---

## 🔌 Legacy Interface

### **RequestRunner Interface**

**Package**: `HTTP`  
**Purpose**: Legacy interface for request handling (deprecated in favor of HttpRequestHandler).

#### **Methods**

```java
@FunctionalInterface
public interface RequestRunner {
    void run(Socket clientSocket) throws IOException;  // Handles client socket
}
```

---

## 📋 Usage Examples

### **Complete Server Setup**

```java
public class CustomServer {
    public static void main(String[] args) {
        try {
            // Create server with custom configuration
            ServerConfig config = new ServerConfig(9000);
            Server server = new Server(config.getPort());

            // Start server
            server.start();

        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        }
    }
}
```

### **Custom Request Handler**

```java
public class ApiHandler implements HttpRequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        // Parse request
        String path = request.getUri().getPath();

        // Handle different API endpoints
        switch (path) {
            case "/api/users":
                return handleUsers(request);
            case "/api/posts":
                return handlePosts(request);
            default:
                return ErrorHandler.createNotFoundResponse(path);
        }
    }

    private HttpResponse handleUsers(HttpRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("application/json"));

        String responseBody = "{\"users\": []}";
        return new HttpResponse(200, headers, responseBody);
    }

    private HttpResponse handlePosts(HttpRequest request) {
        // Implementation for posts endpoint
        return new HttpResponse(200, new HashMap<>(), "Posts endpoint");
    }
}
```

### **Static File Serving with Custom Directory**

```java
// Create static file handler with custom directory
StaticFileHandler fileHandler = new StaticFileHandler("/var/www/html");

// Check if file can be served
if (fileHandler.canServe("styles.css")) {
    HttpResponse response = fileHandler.serveFile("styles.css");
    // Send response to client
}

// List available files
String[] files = fileHandler.listFiles();
for (String file : files) {
    System.out.println("Available file: " + file);
}
```

### **Error Handling with Custom Messages**

```java
// Create custom error responses
HttpResponse notFound = ErrorHandler.createNotFoundResponse("/api/unknown");
HttpResponse badRequest = ErrorHandler.createBadRequestResponse("Missing required parameter");
HttpResponse serverError = ErrorHandler.createInternalServerErrorResponse(new Exception("Database connection failed"));

// Check error types
if (ErrorHandler.isClientError(404)) {
    System.out.println("This is a client error");
}

if (ErrorHandler.isServerError(500)) {
    System.out.println("This is a server error");
}
```

---

## 🔒 Security Considerations

### **Input Validation**

- All HTTP requests are validated before processing
- URI paths are checked for directory traversal attempts
- Request headers are sanitized

### **File Access Security**

- Static file serving prevents access to parent directories
- File permissions are checked before serving
- MIME types are validated

### **Error Information**

- Production mode hides sensitive error details
- Stack traces are not exposed to clients
- Generic error messages for security

---

## 📈 Performance Features

### **Connection Pooling**

- Configurable thread pool for handling connections
- Efficient connection reuse
- Graceful connection termination

### **Caching**

- Static file caching headers
- Configurable cache duration
- Memory-efficient file serving

### **Monitoring**

- Real-time performance metrics
- Request/response timing
- Error rate tracking
- Endpoint usage statistics

---

## 🔄 Extension Points

### **Adding New HTTP Methods**

```java
// Add new method to HttpMethod enum
public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE, CUSTOM
}
```

### **Adding New MIME Types**

```java
// Extend StaticFileHandler.initializeMimeTypes()
private Map<String, String> initializeMimeTypes() {
    Map<String, String> types = new HashMap<>();
    // Add existing types...

    // Add new types
    types.put("mp4", "video/mp4");
    types.put("webm", "video/webm");

    return types;
}
```

### **Custom Error Pages**

```java
// Extend ErrorHandler.createErrorHtml()
private static String createErrorHtml(int statusCode, String statusMessage, String errorMessage) {
    // Custom HTML template with your branding
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head><title>%d %s</title></head>
        <body>
            <h1>%d %s</h1>
            <p>%s</p>
            <!-- Custom styling and branding -->
        </body>
        </html>
        """, statusCode, statusMessage, statusCode, statusMessage, errorMessage);
}
```

---

## 📚 Additional Resources

### **Related Documentation**

- [Project README](../README.md) - Project overview and setup
- [Configuration Guide](../docs/CONFIGURATION.md) - Detailed configuration options
- [Deployment Guide](../docs/DEPLOYMENT.md) - Production deployment instructions

### **External References**

- [Java Documentation](https://docs.oracle.com/) - Official Java documentation
- [HTTP Specification](https://tools.ietf.org/html/rfc7230) - HTTP/1.1 RFC
- [Maven Documentation](https://maven.apache.org/) - Build tool documentation

---

_Last updated: August 2024_  
_Version: 1.0_
