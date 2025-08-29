package HTTP;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class handleRequest {
    private final Map<String, RequestRunner> routes;
    
    public handleRequest(Map<String, RequestRunner> routes) {
        this.routes = routes;
    }
    
    public void handleRequest(final HttpRequest request, final BufferedWriter bufferedWriter) throws IOException {
        final String routeKey = request.getHttpMethod().name().concat(request.getUri().getRawPath());

        if (routes.containsKey(routeKey)) {
            // We'll implement this in the next step
            System.out.println("Handling request for route: " + routeKey);
        } else {
            // Not found - we'll implement proper response in the next step
            System.out.println("Route not found: " + routeKey);
        }
    }
}
