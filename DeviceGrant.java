//DEPS com.fasterxml.jackson.core:jackson-databind:2.13.2

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class DeviceGrant {

    public static void main(String[] args) throws Exception {

        // update these values
        String clientId = "${client-id}" ;
        String issuer = "${issuer}";

        HttpClient client = HttpClient.newHttpClient();

        // List of scopes requested by your application (oauth scopes are space seperated)
        String scopes = String.join(" ", "openid", "profile", "offline_access");

        // HTTP POST form arguments
        Map<String, String> authArgs = Map.of("client_id", clientId,
                                              "scope", scopes); // the arg name IS the singular form of scope

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(issuer + "/v1/device/authorize"))
                .POST(formBody(authArgs))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<Map> httpResponse = client.send(request, rs -> parseJson(Map.class));

        if (httpResponse.statusCode() != 200) {
            System.err.println("Error: " + httpResponse);
            throw new RuntimeException("Failed to initialize device grant authorization, likely " +
                                       "caused by an invalid IdP configuration");
        }

        Map authorizationResponse = httpResponse.body();

        String deviceCode = (String) authorizationResponse.get("device_code");
        String verificationUri = (String) authorizationResponse.get("verification_uri");
        String userCode = (String) authorizationResponse.get("user_code");
        String verificationUriComplete = (String) authorizationResponse.get("verification_uri_complete");
        Duration interval = Duration.ofSeconds((int) authorizationResponse.get("interval"));
        Duration expiresIn = Duration.ofSeconds((int) authorizationResponse.get("expires_in"));

        if (!GraphicsEnvironment.isHeadless() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            System.out.println("Opening browser to: " + verificationUriComplete);
            Desktop.getDesktop().browse(URI.create(verificationUriComplete));
        } else {
            System.out.println("Open a browser and go to: " + verificationUri +
                               "  enter the code: "+ userCode);
        }

        // continue to poll until timeout
        long pollUntilMillis = System.currentTimeMillis() + expiresIn.toMillis();
        while (System.currentTimeMillis() < pollUntilMillis) {

            // first sleep, give the user time to log in!
            System.out.println("Sleeping for " + interval.getSeconds() + " seconds");
            Thread.sleep(interval.toMillis());

            Map<String, String> tokenArgs = Map.of("client_id", clientId,
                    "grant_type","urn:ietf:params:oauth:grant-type:device_code",
                    "device_code", deviceCode);

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(issuer + "/v1/token"))
                    .POST(formBody(tokenArgs))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<Map> tokenResponse = client.send(tokenRequest, rs -> parseJson(Map.class));

            if (tokenResponse.statusCode() == 200) {
                // Do something with the tokens
                String accessToken = (String) tokenResponse.body().get("access_token");
                System.out.println("Authorization complete!");
                System.out.println(tokenResponse.body());

                HttpRequest.newBuilder()
                        .header("Authorization", "Bearer " + accessToken);

                break;
            } else {
                // error, keep polling until timeout
                System.out.println("Error: " + tokenResponse.body());
                System.out.println(tokenResponse);
            }
        }
    }

    private static HttpRequest.BodyPublisher formBody(Map<String, String> params) {
        // Wrap an encoded String in a BodyPublisher
        return HttpRequest.BodyPublishers.ofString(
            // url encode <key>=<value>&
            params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                              + "="
                              + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(joining("&")));
    }

    public static <W> HttpResponse.BodySubscriber<W> parseJson(Class<W> targetType) {
        return HttpResponse.BodySubscribers.mapping(
            HttpResponse.BodySubscribers.ofByteArray(),
            (byte[] bytes) -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue(bytes, targetType);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }
}