package HTTP;

import java.util.Map;

public class HttpHandler {

        private final Map<String, RequestRunner> routes;

        public HttpHandler(final Map<String, RequestRunner> routes) {
            this.routes = routes;
        }

}
