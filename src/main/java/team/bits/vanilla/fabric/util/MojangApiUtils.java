package team.bits.vanilla.fabric.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.UUID;

public class MojangApiUtils {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static boolean checkUsernameIsValid(String username) {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(String.format("https://api.mojang.com/users/profiles/minecraft/%s", username)))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to make HTTP request for username check", ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException("HTTP request for username check was interrupted", ex);
        }

        // If response is not empty, Mojang found a player, return true, else, return false;
        return !response.body().isBlank();
    }

    public static @NotNull String getUsernameFromUUID(@NotNull UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", uuid)))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to make HTTP request for player profile", ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException("HTTP request for player profile was interrupted", ex);
        }

        JsonObject jsonResponse = GSON.fromJson(response.body(), JsonObject.class);

        if (!jsonResponse.has("name")) {
            throw new RuntimeException(String.format("Unable to find username for player %s", uuid));
        }

        return Objects.requireNonNull(jsonResponse.get("name").getAsString());
    }
}
