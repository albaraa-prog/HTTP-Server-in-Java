package HTTP.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import HTTP.ErrorHandling.ErrorHandler;
import HTTP.ErrorHandling.ServerLogger;
import HTTP.Protocol.HttpRequest;
import HTTP.Protocol.HttpResponse;
import HTTP.Request.HttpDecoder;
import HTTP.Request.HttpRequestHandler;
import HTTP.Static.StaticFileHandler;

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
        
        // Add calculator endpoint
        routes.put("POST/calculate", new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                return handleCalculation(request);
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
     * Handles calculation requests and returns the result.
     * 
     * @param request the HTTP request containing the calculation expression
     * @return HttpResponse with calculation result or error
     */
    private HttpResponse handleCalculation(HttpRequest request) {
        try {
            // Get the request body
            String body = request.getBody();
            if (body == null) {
                body = "";
            }
            
            // Parse JSON request
            String expression = parseJsonExpression(body);
            if (expression == null || expression.trim().isEmpty()) {
                return createErrorResponse(400, "Invalid request: expression is required");
            }
            
            // Validate and calculate
            double result = calculateExpression(expression);
            
            // Create JSON response
            String jsonResponse = String.format("{\"result\": %.10g, \"expression\": \"%s\"}", 
                                              result, expression.replace("\"", "\\\""));
            
            Map<String, java.util.List<String>> headers = new HashMap<>();
            headers.put("Content-Type", java.util.List.of("application/json"));
            headers.put("Content-Length", java.util.List.of(String.valueOf(jsonResponse.length())));
            headers.put("Access-Control-Allow-Origin", java.util.List.of("*"));
            headers.put("Access-Control-Allow-Methods", java.util.List.of("POST, OPTIONS"));
            headers.put("Access-Control-Allow-Headers", java.util.List.of("Content-Type"));
            
            return new HttpResponse(200, headers, jsonResponse);
            
        } catch (Exception e) {
            logger.logError("Calculation error", e);
            return createErrorResponse(500, "Calculation failed: " + e.getMessage());
        }
    }
    
    /**
     * Parses the JSON expression from the request body.
     * 
     * @param body the request body
     * @return the expression string or null if invalid
     */
    private String parseJsonExpression(String body) {
        try {
            // Simple JSON parsing for {"expression": "..."}
            if (body.contains("\"expression\"")) {
                // Find the start of the expression value
                int expressionKeyStart = body.indexOf("\"expression\"");
                int colonIndex = body.indexOf(":", expressionKeyStart);
                
                if (colonIndex == -1) {
                    return null;
                }
                
                // Skip whitespace after colon
                int valueStart = colonIndex + 1;
                while (valueStart < body.length() && Character.isWhitespace(body.charAt(valueStart))) {
                    valueStart++;
                }
                
                // Look for opening quote
                if (valueStart >= body.length() || body.charAt(valueStart) != '"') {
                    return null;
                }
                
                int quoteStart = valueStart;
                int quoteEnd = body.indexOf("\"", quoteStart + 1);
                
                if (quoteEnd != -1 && quoteEnd > quoteStart + 1) {
                    return body.substring(quoteStart + 1, quoteEnd);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Calculates a mathematical expression safely.
     * 
     * @param expression the mathematical expression
     * @return the calculation result
     * @throws IllegalArgumentException if the expression is invalid
     */
    private double calculateExpression(String expression) {
        // Remove whitespace
        expression = expression.replaceAll("\\s+", "");
        
        // Check if expression is empty
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }
        
        // Validate expression contains only allowed characters (including scientific notation and 'x' for multiplication)
        // Allow digits, operators, parentheses, decimal points, and scientific notation
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!Character.isDigit(c) && c != '+' && c != '-' && c != '*' && c != '/' && c != '(' && c != ')' && c != '.' && c != 'e' && c != 'E' && c != 'x') {
                throw new IllegalArgumentException("Invalid character '" + c + "' at position " + i + " in expression: '" + expression + "'");
            }
        }
        
        // Convert 'x' to '*' for multiplication
        expression = expression.replace('x', '*');
        
        // Check for balanced parentheses
        int parenCount = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') parenCount++;
            if (c == ')') parenCount--;
            if (parenCount < 0) {
                throw new IllegalArgumentException("Unbalanced parentheses");
            }
        }
        if (parenCount != 0) {
            throw new IllegalArgumentException("Unbalanced parentheses");
        }
        
        // Check for division by zero patterns
        if (expression.contains("/0") && !expression.contains("/0.")) {
            throw new IllegalArgumentException("Division by zero");
        }
        
        // Use a simple expression evaluator
        return evaluateExpression(expression);
    }
    
    /**
     * Evaluates a mathematical expression using a simple recursive descent parser.
     * 
     * @param expression the expression to evaluate
     * @return the result
     */
    private double evaluateExpression(String expression) {
        // Remove all whitespace
        expression = expression.replaceAll("\\s+", "");
        
        // Handle parentheses
        while (expression.contains("(")) {
            int openParen = expression.lastIndexOf("(");
            int closeParen = findMatchingCloseParen(expression, openParen);
            
            if (closeParen == -1) {
                throw new IllegalArgumentException("Unbalanced parentheses");
            }
            
            String innerExpression = expression.substring(openParen + 1, closeParen);
            double innerResult = evaluateExpression(innerExpression);
            
            expression = expression.substring(0, openParen) + 
                        String.format("%.10g", innerResult) + 
                        expression.substring(closeParen + 1);
        }
        
        // Evaluate multiplication and division
        while (expression.contains("*") || expression.contains("/")) {
            int mulIndex = expression.indexOf("*");
            int divIndex = expression.indexOf("/");
            
            int opIndex;
            if (mulIndex == -1) opIndex = divIndex;
            else if (divIndex == -1) opIndex = mulIndex;
            else opIndex = Math.min(mulIndex, divIndex);
            
            double left = getLeftOperand(expression, opIndex);
            double right = getRightOperand(expression, opIndex);
            
            double result;
            if (expression.charAt(opIndex) == '*') {
                result = left * right;
            } else {
                if (right == 0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                result = left / right;
            }
            
            expression = replaceOperation(expression, opIndex, result);
        }
        
        // Evaluate addition and subtraction
        while (expression.contains("+") || (expression.contains("-") && !expression.startsWith("-"))) {
            int addIndex = expression.indexOf("+");
            int subIndex = expression.indexOf("-", 1); // Skip leading minus
            
            int opIndex;
            if (addIndex == -1) opIndex = subIndex;
            else if (subIndex == -1) opIndex = addIndex;
            else opIndex = Math.min(addIndex, subIndex);
            
            double left = getLeftOperand(expression, opIndex);
            double right = getRightOperand(expression, opIndex);
            
            double result;
            if (expression.charAt(opIndex) == '+') {
                result = left + right;
            } else {
                result = left - right;
            }
            
            expression = replaceOperation(expression, opIndex, result);
        }
        
        // Parse final number (including scientific notation)
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expression");
        }
    }
    
    private int findMatchingCloseParen(String expression, int openParen) {
        int count = 1;
        for (int i = openParen + 1; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') count++;
            if (expression.charAt(i) == ')') count--;
            if (count == 0) return i;
        }
        return -1;
    }
    
    private double getLeftOperand(String expression, int opIndex) {
        int start = opIndex - 1;
        while (start >= 0 && (Character.isDigit(expression.charAt(start)) || 
                              expression.charAt(start) == '.' || 
                              expression.charAt(start) == '-' ||
                              expression.charAt(start) == 'e' ||
                              expression.charAt(start) == 'E')) {
            start--;
        }
        if (start < 0 || expression.charAt(start) == '+' || expression.charAt(start) == '-' || 
            expression.charAt(start) == '*' || expression.charAt(start) == '/') {
            start++;
        }
        return Double.parseDouble(expression.substring(start, opIndex));
    }
    
    private double getRightOperand(String expression, int opIndex) {
        int end = opIndex + 1;
        if (expression.charAt(end) == '-') end++; // Handle negative numbers
        while (end < expression.length() && (Character.isDigit(expression.charAt(end)) || 
                                            expression.charAt(end) == '.' ||
                                            expression.charAt(end) == 'e' ||
                                            expression.charAt(end) == 'E')) {
            end++;
        }
        return Double.parseDouble(expression.substring(opIndex + 1, end));
    }
    
    private String replaceOperation(String expression, int opIndex, double result) {
        int leftStart = opIndex - 1;
        while (leftStart >= 0 && (Character.isDigit(expression.charAt(leftStart)) || 
                                 expression.charAt(leftStart) == '.' || 
                                 expression.charAt(leftStart) == '-' ||
                                 expression.charAt(leftStart) == 'e' ||
                                 expression.charAt(leftStart) == 'E')) {
            leftStart--;
        }
        if (leftStart < 0 || expression.charAt(leftStart) == '+' || expression.charAt(leftStart) == '-' || 
            expression.charAt(leftStart) == '*' || expression.charAt(leftStart) == '/') {
            leftStart++;
        }
        
        int rightEnd = opIndex + 1;
        if (expression.charAt(rightEnd) == '-') rightEnd++; // Handle negative numbers
        while (rightEnd < expression.length() && (Character.isDigit(expression.charAt(rightEnd)) || 
                                                 expression.charAt(rightEnd) == '.' ||
                                                 expression.charAt(rightEnd) == 'e' ||
                                                 expression.charAt(rightEnd) == 'E')) {
            rightEnd++;
        }
        
        return expression.substring(0, leftStart) + 
               String.format("%.10g", result) + 
               expression.substring(rightEnd);
    }
    
    /**
     * Creates an error response with JSON format.
     * 
     * @param statusCode the HTTP status code
     * @param message the error message
     * @return HttpResponse with error
     */
    private HttpResponse createErrorResponse(int statusCode, String message) {
        String jsonResponse = String.format("{\"error\": \"%s\"}", message.replace("\"", "\\\""));
        
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("application/json"));
        headers.put("Content-Length", java.util.List.of(String.valueOf(jsonResponse.length())));
        headers.put("Access-Control-Allow-Origin", java.util.List.of("*"));
        headers.put("Access-Control-Allow-Methods", java.util.List.of("POST, OPTIONS"));
        headers.put("Access-Control-Allow-Headers", java.util.List.of("Content-Type"));
        
        return new HttpResponse(statusCode, headers, jsonResponse);
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