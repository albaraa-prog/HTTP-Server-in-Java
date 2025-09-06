package HTTP.Request;

import HTTP.Protocol.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class Routes {
    private final Map<String, RequestRunner> routes;
    
    public Routes() {
        this.routes = new HashMap<>();
    }
    
    public void addRoute(HttpMethod opCode, String route, RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }
    
    public Map<String, RequestRunner> getRoutes() {
        return routes;
    }
}
