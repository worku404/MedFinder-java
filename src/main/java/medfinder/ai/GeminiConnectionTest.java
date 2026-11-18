package medfinder.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class GeminiConnectionTest {

    private static final String DEFAULT_MODEL = "gemini-flash-lite-latest";
    private static final String ENV_VAR = "GEMINI_API_KEY";
    private static final String MODEL_ENV_VAR = "GEMINI_MODEL";

    public static void main(String[] args) {
        String apiKey = resolveApiKey(args);
        String model = resolveSetting(MODEL_ENV_VAR, DEFAULT_MODEL);
        if (apiKey.isBlank()) {
            System.out.println("No API key found.");
            System.out.println("Get one at https://aistudio.google.com/apikey (it starts with AIza...)");
            System.out.println("Then copy .env.example to .env and put your key there, or set " + ENV_VAR + ".");
            return;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        String jsonPayload = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "Hello! Send a short proof-of-concept confirmation." }
                      ]
                    }
                  ]
                }
                """;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            System.out.println("Sending prompt to " + model + "...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Request failed. HTTP " + response.statusCode());
                System.out.println(response.body());
                return;
            }

            System.out.println("HTTP 200 - connection works.");
            System.out.println("Gemini says: " + extractFirstText(response.body()));
        } catch (Exception e) {
            System.out.println("Could not reach Gemini: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static String resolveApiKey(String[] args) {
        String fromEnv = System.getenv(ENV_VAR);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        if (args.length > 0 && !args[0].isBlank()) {
            return args[0].trim();
        }
        return readDotEnv(ENV_VAR);
    }

    private static String resolveSetting(String key, String fallback) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromFile = readDotEnv(key);
        return fromFile.isBlank() ? fallback : fromFile;
    }

    private static String readDotEnv(String key) {
        Path envFile = Path.of(".env");
        if (!Files.isRegularFile(envFile)) {
            return "";
        }
        try {
            for (String line : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int equals = trimmed.indexOf('=');
                if (equals <= 0) {
                    continue;
                }
                if (!trimmed.substring(0, equals).trim().equals(key)) {
                    continue;
                }
                String value = trimmed.substring(equals + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        } catch (IOException e) {
            System.out.println("Could not read .env: " + e.getMessage());
        }
        return "";
    }

    private static String extractFirstText(String json) {
        int textKey = json.indexOf("\"text\"");
        if (textKey < 0) {
            return "(no text field in response)";
        }
        int startQuote = json.indexOf('"', json.indexOf(':', textKey) + 1);
        if (startQuote < 0) {
            return "(could not parse text field)";
        }

        StringBuilder out = new StringBuilder();
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                break;
            }
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(++i);
                switch (next) {
                    case 'n' -> out.append('\n');
                    case 't' -> out.append('\t');
                    case 'r' -> out.append('\r');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            out.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                    }
                    default -> out.append(next);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
