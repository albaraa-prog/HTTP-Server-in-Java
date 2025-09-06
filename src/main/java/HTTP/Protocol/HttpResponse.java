package HTTP.Protocol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, List<String>> responseHeaders;
    private final Object entity;
    
    public HttpResponse(int statusCode, Map<String, List<String>> responseHeaders, Object entity) {
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.entity = entity;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }
    
    public Optional<Object> getEntity() {
        return Optional.ofNullable(entity);
    }
}
