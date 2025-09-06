package HTTP.ErrorHandling;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server logging and monitoring system.
 * Provides structured logging, request tracking, and performance metrics.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class ServerLogger {
    
    private static final Logger logger = Logger.getLogger(ServerLogger.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Performance metrics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> endpointRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicLong> statusCodeCounts = new ConcurrentHashMap<>();
    
    private final boolean enableLogging;
    private final boolean enableMonitoring;
    
    /**
     * Creates a new ServerLogger with specified settings.
     * 
     * @param enableLogging whether to enable logging
     * @param enableMonitoring whether to enable monitoring
     */
    public ServerLogger(boolean enableLogging, boolean enableMonitoring) {
        this.enableLogging = enableLogging;
        this.enableMonitoring = enableMonitoring;
    }
    
    /**
     * Logs a server startup message.
     * 
     * @param port the port the server is listening on
     */
    public void logServerStart(int port) {
        if (enableLogging) {
            String message = String.format("🚀 HTTP Server started on port %d at %s", 
                port, getCurrentTimestamp());
            System.out.println(message);
            logger.info(message);
        }
    }
    
    /**
     * Logs a server shutdown message.
     */
    public void logServerStop() {
        if (enableLogging) {
            String message = String.format("🛑 HTTP Server stopped at %s", getCurrentTimestamp());
            System.out.println(message);
            logger.info(message);
        }
    }
    
    /**
     * Logs a new connection.
     * 
     * @param clientSocket the client socket
     */
    public void logConnection(Socket clientSocket) {
        if (enableLogging) {
            String message = String.format("🔌 New connection from %s:%d at %s", 
                clientSocket.getInetAddress().getHostAddress(),
                clientSocket.getPort(),
                getCurrentTimestamp());
            System.out.println(message);
            logger.info(message);
        }
    }
    
    /**
     * Logs a request with timing information.
     * 
     * @param method the HTTP method
     * @param uri the request URI
     * @param statusCode the response status code
     * @param responseTime the response time in milliseconds
     */
    public void logRequest(String method, String uri, int statusCode, long responseTime) {
        if (enableLogging) {
            String message = String.format("📝 %s %s -> %d (%dms) at %s", 
                method, uri, statusCode, responseTime, getCurrentTimestamp());
            System.out.println(message);
            logger.info(message);
        }
        
        if (enableMonitoring) {
            updateMetrics(method, uri, statusCode, responseTime);
        }
    }
    
    /**
     * Logs an error.
     * 
     * @param message the error message
     * @param throwable the exception (optional)
     */
    public void logError(String message, Throwable throwable) {
        if (enableLogging) {
            String errorMessage = String.format("❌ ERROR: %s at %s", message, getCurrentTimestamp());
            System.err.println(errorMessage);
            logger.log(Level.SEVERE, errorMessage, throwable);
        }
    }
    
    /**
     * Logs a warning.
     * 
     * @param message the warning message
     */
    public void logWarning(String message) {
        if (enableLogging) {
            String warningMessage = String.format("⚠️  WARNING: %s at %s", message, getCurrentTimestamp());
            System.out.println(warningMessage);
            logger.warning(warningMessage);
        }
    }
    
    /**
     * Logs an info message.
     * 
     * @param message the info message
     */
    public void logInfo(String message) {
        if (enableLogging) {
            String infoMessage = String.format("ℹ️  INFO: %s at %s", message, getCurrentTimestamp());
            System.out.println(infoMessage);
            logger.info(infoMessage);
        }
    }
    
    /**
     * Updates performance metrics.
     * 
     * @param method the HTTP method
     * @param uri the request URI
     * @param statusCode the response status code
     * @param responseTime the response time in milliseconds
     */
    private void updateMetrics(String method, String uri, int statusCode, long responseTime) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
        
        if (statusCode >= 200 && statusCode < 400) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        
        // Track endpoint requests
        String endpoint = method + " " + uri;
        endpointRequests.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        
        // Track status code counts
        statusCodeCounts.computeIfAbsent(statusCode, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Gets current performance metrics.
     * 
     * @return a formatted string with current metrics
     */
    public String getMetrics() {
        if (!enableMonitoring) {
            return "Monitoring is disabled";
        }
        
        long total = totalRequests.get();
        long successful = successfulRequests.get();
        long failed = failedRequests.get();
        long avgResponseTime = total > 0 ? totalResponseTime.get() / total : 0;
        
        StringBuilder metrics = new StringBuilder();
        metrics.append("📊 SERVER METRICS\n");
        metrics.append("================\n");
        metrics.append(String.format("Total Requests: %d\n", total));
        metrics.append(String.format("Successful: %d\n", successful));
        metrics.append(String.format("Failed: %d\n", failed));
        metrics.append(String.format("Success Rate: %.2f%%\n", total > 0 ? (successful * 100.0 / total) : 0));
        metrics.append(String.format("Average Response Time: %dms\n", avgResponseTime));
        metrics.append(String.format("Timestamp: %s\n", getCurrentTimestamp()));
        
        return metrics.toString();
    }
    
    /**
     * Gets the current timestamp.
     * 
     * @return formatted timestamp string
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
    
    /**
     * Resets all metrics counters.
     */
    public void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        endpointRequests.clear();
        statusCodeCounts.clear();
        logInfo("Metrics reset");
    }
}
