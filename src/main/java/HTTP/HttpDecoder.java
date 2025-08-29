package HTTP;

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

public class HttpDecoder {

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
            String version = parts[2];
            
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
            
            // Create HttpRequest
            HttpRequest request = new HttpRequest.Builder()
                    .setHttpMethod(httpMethod)
                    .setUri(requestUri)
                    .setRequestHeaders(headers)
                    .build();
            
            return Optional.of(request);
            
        } catch (IOException e) {
            System.err.println("Error reading HTTP request: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    private static HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
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
}