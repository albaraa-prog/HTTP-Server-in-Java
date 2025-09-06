package HTTP.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class buildHeaderStrings {
    
    public static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders) {
        final List<String> responseHeadersList = new ArrayList<>();

        responseHeaders.forEach((name, values) -> {
            final StringBuilder valuesCombined = new StringBuilder();
            values.forEach(valuesCombined::append);
            valuesCombined.append(";");

            responseHeadersList.add(name + ": " + valuesCombined + "\r\n");
        });

        return responseHeadersList;
    }
}