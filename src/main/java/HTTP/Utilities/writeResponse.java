package HTTP.Utilities;

import HTTP.Protocol.HttpResponse;
import HTTP.Protocol.HttpStatusCode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class writeResponse {
    
    public static void writeResponse(final BufferedWriter outputStream, final HttpResponse response) throws IOException {
        try {
            final int statusCode = response.getStatusCode();
            final String statusCodeMeaning = HttpStatusCode.STATUS_CODES.get(statusCode);
            final List<String> responseHeaders = buildHeaderStrings.buildHeaderStrings(response.getResponseHeaders());

            outputStream.write("HTTP/1.1 " + statusCode + " " + statusCodeMeaning + "\r\n");

            for (String header : responseHeaders) {
                outputStream.write(header);
            }

            final Optional<String> entityString = response.getEntity().flatMap(getResponseString::getResponseString);
            if (entityString.isPresent()) {
                final String encodedString = new String(entityString.get().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                outputStream.write("Content-Length: " + encodedString.getBytes().length + "\r\n");
                outputStream.write("\r\n");
                outputStream.write(encodedString);
            } else {
                outputStream.write("\r\n");
            }
        } catch (Exception e) {
            System.out.println("Write Response: " + e.getMessage());
        }
    }
}
