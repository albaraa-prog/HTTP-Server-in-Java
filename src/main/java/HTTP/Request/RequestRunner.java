package HTTP.Request;

import java.io.IOException;
import java.net.Socket;

@FunctionalInterface
public interface RequestRunner {
    void run(Socket clientSocket) throws IOException;
}
