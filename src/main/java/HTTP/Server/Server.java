package HTTP;

import HTTP.ErrorHandling.ErrorHandler;
import HTTP.ErrorHandling.ServerLogger;
import HTTP.Protocol.HttpRequest;
import HTTP.Protocol.HttpResponse;
import HTTP.Request.HttpDecoder;
import HTTP.Request.HttpRequestHandler;
import HTTP.Static.StaticFileHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main HTTP server class with integrated Phase 3 features.
 * Handles multi-threaded connections, static file serving, and logging.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class Server {

    private final Map<String, HttpRequestHandler> routes;
    private final ServerSocket socket;
    private final Executor threadPool;
    private final ServerConfig config;
    private final ServerLogger logger;
    private final StaticFileHandler staticFileHandler;
    private boolean running;

    /**
     * Creates a new HTTP server with the specified port.
     * 
     * @param port the port to listen on
     * @throws IOException if the server socket cannot be created
     */
    public Server(int port) throws IOException {
        this.config = new ServerConfig(port);
        this.logger = new ServerLogger(config.isLoggingEnabled(), config.isMonitoringEnabled());
        this.staticFileHandler = new StaticFileHandler(config.getStaticDirectory());
        this.routes = new HashMap<>();
        this.threadPool = Executors.newFixedThreadPool(config.getThreadPoolSize());
        this.socket = new ServerSocket(port);
        this.running = false;
        
        // Initialize default routes
        initializeDefaultRoutes();
    }
    
    /**
     * Creates a new HTTP server with default configuration.
     * 
     * @throws IOException if the server socket cannot be created
     */
    public Server() throws IOException {
        this(8080); // Default port
    }
    
    /**
     * Initializes default routes including static file serving.
     */
    private void initializeDefaultRoutes() {
        // Add static file serving route
        routes.put("GET/", new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                return serveStaticFile(request.getUri().getPath());
            }
        });
        
        // Add metrics endpoint
        routes.put("GET/metrics", new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                return createMetricsResponse();
            }
        });
        
        // Add configuration endpoint
        routes.put("GET/config", new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                return createConfigResponse();
            }
        });
        
        // Add file listing endpoint
        routes.put("GET/files", new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                return createFileListingResponse();
            }
        });
    }
    
    /**
     * Serves static files from the configured directory.
     * 
     * @param path the requested file path
     * @return HttpResponse with file content or error
     */
    private HttpResponse serveStaticFile(String path) {
        // Handle root path
        if (path.equals("/") || path.isEmpty()) {
            path = "index.html";
        }
        
        // Remove leading slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        if (staticFileHandler.canServe(path)) {
            return staticFileHandler.serveFile(path);
        } else {
            return ErrorHandler.createNotFoundResponse(path);
        }
    }
    
    /**
     * Creates a response with server metrics.
     * 
     * @return HttpResponse with metrics
     */
    private HttpResponse createMetricsResponse() {
        String metrics = logger.getMetrics();
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("text/plain"));
        headers.put("Content-Length", java.util.List.of(String.valueOf(metrics.length())));
        
        return new HttpResponse(200, headers, metrics);
    }
    
    /**
     * Creates a response with server configuration.
     * 
     * @return HttpResponse with configuration
     */
    private HttpResponse createConfigResponse() {
        StringBuilder configInfo = new StringBuilder();
        configInfo.append("HTTP Server Configuration\n");
        configInfo.append("========================\n");
        configInfo.append("Port: ").append(config.getPort()).append("\n");
        configInfo.append("Static Directory: ").append(config.getStaticDirectory()).append("\n");
        configInfo.append("Thread Pool Size: ").append(config.getThreadPoolSize()).append("\n");
        configInfo.append("Logging Enabled: ").append(config.isLoggingEnabled()).append("\n");
        configInfo.append("Monitoring Enabled: ").append(config.isMonitoringEnabled()).append("\n");
        
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("text/plain"));
        headers.put("Content-Length", java.util.List.of(String.valueOf(configInfo.length())));
        
        return new HttpResponse(200, headers, configInfo.toString());
    }
    
    /**
     * Creates a response with file listing.
     * 
     * @return HttpResponse with file listing
     */
    private HttpResponse createFileListingResponse() {
        String[] files = staticFileHandler.listFiles();
        StringBuilder fileList = new StringBuilder();
        fileList.append("Static Files Available\n");
        fileList.append("=====================\n");
        
        if (files.length == 0) {
            fileList.append("No files found in ").append(config.getStaticDirectory()).append("\n");
        } else {
            for (String file : files) {
                fileList.append("- ").append(file).append("\n");
            }
        }
        
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("text/plain"));
        headers.put("Content-Length", java.util.List.of(String.valueOf(fileList.length())));
        
        return new HttpResponse(200, headers, fileList.toString());
    }
    
    /**
     * Starts the HTTP server.
     */
    public void start() {
        running = true;
        logger.logServerStart(socket.getLocalPort());
        
        while (running) {
            try {
                Socket clientSocket = socket.accept();
                logger.logConnection(clientSocket);
                threadPool.execute(() -> handleConnection(clientSocket));
            } catch (IOException e) {
                if (running) {
                    logger.logError("Error accepting connection", e);
                }
            }
        }
    }
    
    /**
     * Handles a client connection.
     * 
     * @param clientSocket the client socket
     */
    private void handleConnection(Socket clientSocket) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Parse HTTP request
            java.util.Optional<HttpRequest> requestOpt = HttpDecoder.decode(clientSocket.getInputStream());
            
            if (requestOpt.isPresent()) {
                HttpRequest request = requestOpt.get();
                
                // Find matching route
                String routeKey = request.getHttpMethod().name() + request.getUri().getPath();
                HttpRequestHandler handler = routes.get(routeKey);
                
                HttpResponse response;
                if (handler != null) {
                    response = handler.handle(request);
                } else {
                    // Try static file serving as fallback
                    response = serveStaticFile(request.getUri().getPath());
                }
                
                // Send response
                writeResponse(clientSocket, response);
                
                // Log request with timing
                long responseTime = System.currentTimeMillis() - startTime;
                logger.logRequest(request.getHttpMethod().name(), request.getUri().getPath(), 
                               response.getStatusCode(), responseTime);
                
            } else {
                // Invalid request
                HttpResponse errorResponse = ErrorHandler.createBadRequestResponse("Invalid HTTP request");
                writeResponse(clientSocket, errorResponse);
                logger.logError("Invalid HTTP request received", null);
            }
            
        } catch (Exception e) {
            logger.logError("Error handling connection", e);
            try {
                HttpResponse errorResponse = ErrorHandler.createInternalServerErrorResponse(e);
                writeResponse(clientSocket, errorResponse);
            } catch (Exception ex) {
                logger.logError("Error sending error response", ex);
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.logError("Error closing client socket", e);
            }
        }
    }
    
    /**
     * Writes an HTTP response to the client socket.
     * 
     * @param clientSocket the client socket
     * @param response the response to send
     * @throws IOException if writing fails
     */
    private void writeResponse(Socket clientSocket, HttpResponse response) throws IOException {
        java.io.OutputStream output = clientSocket.getOutputStream();
        
        // Write status line
        String statusLine = "HTTP/1.1 " + response.getStatusCode() + " " + 
                           ErrorHandler.getStatusMessage(response.getStatusCode()) + "\r\n";
        output.write(statusLine.getBytes());
        
        // Write headers
        for (Map.Entry<String, java.util.List<String>> header : response.getResponseHeaders().entrySet()) {
            for (String value : header.getValue()) {
                String headerLine = header.getKey() + ": " + value + "\r\n";
                output.write(headerLine.getBytes());
            }
        }
        
        // Write empty line to separate headers from body
        output.write("\r\n".getBytes());
        
        // Write body
        if (response.getEntity().isPresent()) {
            Object entity = response.getEntity().get();
            if (entity instanceof String) {
                output.write(((String) entity).getBytes());
            } else if (entity instanceof byte[]) {
                output.write((byte[]) entity);
            }
        }
        
        output.flush();
    }
    
    /**
     * Stops the HTTP server.
     */
    public void stop() {
        running = false;
        logger.logServerStop();
        try {
            socket.close();
        } catch (IOException e) {
            logger.logError("Error closing server", e);
        }
    }
    
    /**
     * Gets the server configuration.
     * 
     * @return the server configuration
     */
    public ServerConfig getConfig() {
        return config;
    }
    
    /**
     * Gets the server logger.
     * 
     * @return the server logger
     */
    public ServerLogger getLogger() {
        return logger;
    }
}