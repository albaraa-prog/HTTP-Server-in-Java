package HTTP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    private final Map<String, RequestRunner> routes;
    private final ServerSocket socket;
    private final Executor threadPool;
    private boolean running;

    public Server(int port) throws IOException {
        routes = new HashMap<>();
        threadPool = Executors.newFixedThreadPool(100);
        socket = new ServerSocket(port);
        running = false;
        
        // Initialize default routes
        initializeDefaultRoutes();
    }
    
    private void initializeDefaultRoutes() {
        // We'll add routes in the next step
    }
    
    public void start() {
        running = true;
        System.out.println("Server listening on port " + socket.getLocalPort());
        
        while (running) {
            try {
                Socket clientSocket = socket.accept();
                threadPool.execute(() -> handleConnection(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleConnection(Socket clientSocket) {
        // Use our enhanced connection handler
        handleConnection.handleConnection(clientSocket);
    }
    
    public void stop() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
}