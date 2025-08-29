package HTTP;

import java.io.InputStream;
import java.util.Optional;

public class HttpDecoder {

    public static Optional<HttpRequest> decode(final InputStream inputStream) {
        // We'll implement proper HTTP decoding in the next step
        System.out.println("Decoding HTTP request...");
        return Optional.empty();
    }
}