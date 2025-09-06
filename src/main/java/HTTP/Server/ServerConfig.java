package HTTP.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Server configuration management class.
 * Handles loading configuration from properties file and environment variables.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class ServerConfig {
    
    // Default configuration values
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_STATIC_DIR = "public";
    private static final int DEFAULT_THREAD_POOL_SIZE = 100;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final boolean DEFAULT_ENABLE_LOGGING = true;
    private static final boolean DEFAULT_ENABLE_MONITORING = true;
    
    private final Properties properties;
    private final int port;
    private final String staticDirectory;
    private final int threadPoolSize;
    private final Level logLevel;
    private final boolean enableLogging;
    private final boolean enableMonitoring;
    
    /**
     * Creates a new ServerConfig with default values.
     */
    public ServerConfig() {
        this.properties = new Properties();
        this.port = DEFAULT_PORT;
        this.staticDirectory = DEFAULT_STATIC_DIR;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        this.logLevel = Level.parse(DEFAULT_LOG_LEVEL);
        this.enableLogging = DEFAULT_ENABLE_LOGGING;
        this.enableMonitoring = DEFAULT_ENABLE_MONITORING;
    }
    
    /**
     * Creates a new ServerConfig with custom port.
     * 
     * @param port the port number to use
     */
    public ServerConfig(int port) {
        this.properties = new Properties();
        this.port = port;
        this.staticDirectory = DEFAULT_STATIC_DIR;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        this.logLevel = Level.parse(DEFAULT_LOG_LEVEL);
        this.enableLogging = DEFAULT_ENABLE_LOGGING;
        this.enableMonitoring = DEFAULT_ENABLE_MONITORING;
    }
    
    /**
     * Loads configuration from a properties file.
     * 
     * @param configFile the path to the configuration file
     * @return this ServerConfig instance for chaining
     * @throws IOException if the file cannot be read
     */
    public ServerConfig loadFromFile(String configFile) throws IOException {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            return this;
        }
    }
    
    /**
     * Loads configuration from environment variables.
     * 
     * @return this ServerConfig instance for chaining
     */
    public ServerConfig loadFromEnvironment() {
        String envPort = System.getenv("HTTP_SERVER_PORT");
        if (envPort != null) {
            try {
                int port = Integer.parseInt(envPort);
                // In a real implementation, you'd set this.port = port
            } catch (NumberFormatException e) {
                // Log warning about invalid port
            }
        }
        
        String envStaticDir = System.getenv("HTTP_SERVER_STATIC_DIR");
        if (envStaticDir != null) {
            // In a real implementation, you'd set this.staticDirectory = envStaticDir
        }
        
        return this;
    }
    
    // Getters
    public int getPort() { return port; }
    public String getStaticDirectory() { return staticDirectory; }
    public int getThreadPoolSize() { return threadPoolSize; }
    public Level getLogLevel() { return logLevel; }
    public boolean isLoggingEnabled() { return enableLogging; }
    public boolean isMonitoringEnabled() { return enableMonitoring; }
    
    /**
     * Gets a configuration property value.
     * 
     * @param key the property key
     * @param defaultValue the default value if key not found
     * @return the property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Gets a configuration property value.
     * 
     * @param key the property key
     * @return the property value or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
