package HTTP.Static;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import HTTP.Protocol.HttpResponse;

/**
 * Handles serving static files from a configured directory.
 * Supports common file types with proper MIME type detection.
 * 
 * @author HTTP Server Team
 * @version 1.0
 */
public class StaticFileHandler {
    
    private final String staticDirectory;
    private final Map<String, String> mimeTypes;
    
    /**
     * Creates a new StaticFileHandler with the specified static directory.
     * 
     * @param staticDirectory the directory containing static files
     */
    public StaticFileHandler(String staticDirectory) {
        this.staticDirectory = staticDirectory;
        this.mimeTypes = initializeMimeTypes();
    }
    
    /**
     * Checks if a file exists and can be served.
     * 
     * @param uri the request URI
     * @return true if the file exists and is readable
     */
    public boolean canServe(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        
        // Security: prevent directory traversal attacks
        if (uri.contains("..") || uri.startsWith("/")) {
            return false;
        }
        
        File file = new File(staticDirectory, uri);
        return file.exists() && file.isFile() && file.canRead();
    }
    
    /**
     * Serves a static file and returns an HttpResponse.
     * 
     * @param uri the request URI
     * @return HttpResponse with file content or error
     */
    public HttpResponse serveFile(String uri) {
        try {
            File file = new File(staticDirectory, uri);
            
            if (!file.exists()) {
                return createErrorResponse(404, "File not found: " + uri);
            }
            
            if (!file.isFile()) {
                return createErrorResponse(400, "Not a file: " + uri);
            }
            
            if (!file.canRead()) {
                return createErrorResponse(403, "Cannot read file: " + uri);
            }
            
            // Read file content
            byte[] content = Files.readAllBytes(file.toPath());
            
            // Determine MIME type
            String mimeType = getMimeType(uri);
            
            // Create response headers
            Map<String, java.util.List<String>> headers = new HashMap<>();
            headers.put("Content-Type", java.util.List.of(mimeType));
            headers.put("Content-Length", java.util.List.of(String.valueOf(content.length)));
            headers.put("Cache-Control", java.util.List.of("public, max-age=3600"));
            
            return new HttpResponse(200, headers, content);
            
        } catch (IOException e) {
            return createErrorResponse(500, "Error reading file: " + e.getMessage());
        }
    }
    
    /**
     * Gets the MIME type for a file based on its extension.
     * 
     * @param uri the file URI
     * @return the MIME type string
     */
    private String getMimeType(String uri) {
        String extension = getFileExtension(uri);
        return mimeTypes.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }
    
    /**
     * Extracts the file extension from a URI.
     * 
     * @param uri the file URI
     * @return the file extension (without the dot)
     */
    private String getFileExtension(String uri) {
        int lastDot = uri.lastIndexOf('.');
        if (lastDot > 0 && lastDot < uri.length() - 1) {
            return uri.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * Creates an error response.
     * 
     * @param statusCode the HTTP status code
     * @param message the error message
     * @return HttpResponse with error details
     */
    private HttpResponse createErrorResponse(int statusCode, String message) {
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("text/plain"));
        
        String errorContent = "Error " + statusCode + ": " + message;
        headers.put("Content-Length", java.util.List.of(String.valueOf(errorContent.length())));
        
        return new HttpResponse(statusCode, headers, errorContent);
    }
    
    /**
     * Initializes the MIME type mapping for common file extensions.
     * 
     * @return Map of file extensions to MIME types
     */
    private Map<String, String> initializeMimeTypes() {
        Map<String, String> types = new HashMap<>();
        
        // Text files
        types.put("html", "text/html");
        types.put("htm", "text/html");
        types.put("css", "text/css");
        types.put("js", "application/javascript");
        types.put("json", "application/json");
        types.put("xml", "application/xml");
        types.put("txt", "text/plain");
        types.put("md", "text/markdown");
        
        // Image files
        types.put("png", "image/png");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("gif", "image/gif");
        types.put("svg", "image/svg+xml");
        types.put("ico", "image/x-icon");
        types.put("webp", "image/webp");
        
        // Document files
        types.put("pdf", "application/pdf");
        types.put("doc", "application/msword");
        types.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        
        // Archive files
        types.put("zip", "application/zip");
        types.put("tar", "application/x-tar");
        types.put("gz", "application/gzip");
        
        return types;
    }
    
    /**
     * Gets the static directory path.
     * 
     * @return the static directory path
     */
    public String getStaticDirectory() {
        return staticDirectory;
    }
    
    /**
     * Lists all available static files.
     * 
     * @return array of file names in the static directory
     */
    public String[] listFiles() {
        File dir = new File(staticDirectory);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(File::isFile);
            if (files != null) {
                String[] fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                return fileNames;
            }
        }
        return new String[0];
    }
}
