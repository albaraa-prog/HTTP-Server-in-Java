package HTTP;

import HTTP.Server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting HTTP Server...");
        
        // Default port 8080 if no port specified
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        
        Server server = new Server(port);
        server.start();
        
        System.out.println("HTTP Server started on port " + port);
        System.out.println("Server is running on http://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server");
    }
}
