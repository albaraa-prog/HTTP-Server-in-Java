package HTTP;

import java.io.IOException;
import java.net.Socket;

public class handleConnection {
    
    public static void handleConnection(Socket clientConnection) {
        try {
            System.out.println("Handling connection from: " + clientConnection.getInetAddress());
            // We'll implement proper request handling in the next step
            clientConnection.close();
        } catch (IOException ignored) {
            System.err.println("Error handling connection");
        }
    }
}
