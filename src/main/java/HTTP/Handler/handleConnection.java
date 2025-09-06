package HTTP.Handler;

import HTTP.Protocol.HttpRequest;
import HTTP.Request.HttpDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class handleConnection {
    
    public static void handleConnection(Socket clientSocket) {
        try {
            System.out.println("Handling connection from: " + clientSocket.getInetAddress());
            
            // Get input and output streams
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            
            // Decode HTTP request
            var requestOpt = HttpDecoder.decode(inputStream);
            if (requestOpt.isPresent()) {
                HttpRequest request = requestOpt.get();
                System.out.println("Received request: " + request.getHttpMethod() + " " + request.getUri());
                
                // Handle the request and generate response
                handleRequest(request, outputStream);
            } else {
                // Send bad request response
                sendErrorResponse(outputStream, 400, "Bad Request");
            }
            
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }
    
    private static void handleRequest(HttpRequest request, OutputStream outputStream) throws IOException {
        // For now, we'll just send a simple response
        // In the next step, we'll implement proper route handling
        String response = "HTTP/1.1 200 OK\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: 25\r\n" +
                         "\r\n" +
                         "Hello from HTTP Server!";
        
        outputStream.write(response.getBytes());
        outputStream.flush();
    }
    
    private static void sendErrorResponse(OutputStream outputStream, int statusCode, String message) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + message + "\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: " + message.length() + "\r\n" +
                         "\r\n" +
                         message;
        
        outputStream.write(response.getBytes());
        outputStream.flush();
    }
}
