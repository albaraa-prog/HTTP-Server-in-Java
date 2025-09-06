package HTTP;

import java.util.HashMap;
import java.util.Map;

public class HttpStatusCode {
    public static final Map<Integer, String> STATUS_CODES = new HashMap<>();
    
    static {
        // 2xx Success
        STATUS_CODES.put(200, "OK");
        STATUS_CODES.put(201, "Created");
        STATUS_CODES.put(204, "No Content");
        
        // 4xx Client Errors
        STATUS_CODES.put(400, "Bad Request");
        STATUS_CODES.put(404, "Not Found");
        STATUS_CODES.put(405, "Method Not Allowed");
        
        // 5xx Server Errors
        STATUS_CODES.put(500, "Internal Server Error");
        STATUS_CODES.put(501, "Not Implemented");
        STATUS_CODES.put(505, "HTTP Version Not Supported");
    }
}
