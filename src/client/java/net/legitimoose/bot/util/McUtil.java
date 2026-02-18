package net.legitimoose.bot.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class McUtil {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static String getUuid(String username) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest bannedUUIDRequest = HttpRequest.newBuilder()
                .uri(new URI(String.format("https://playerdb.co/api/player/minecraft/%s", username)))
                .GET()
                .build();
        String response = client.send(bannedUUIDRequest, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject data = JsonParser.parseString(response).getAsJsonObject();

        return (data.getAsJsonObject("data").getAsJsonObject("player").get("id").getAsString());
    }

    public static String getName(String uuid) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest bannedUUIDRequest = HttpRequest.newBuilder()
                .uri(new URI(String.format("https://playerdb.co/api/player/minecraft/%s", uuid)))
                .GET()
                .build();
        String response = client.send(bannedUUIDRequest, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject data = JsonParser.parseString(response).getAsJsonObject();

        return (data.getAsJsonObject("data").getAsJsonObject("player").get("username").getAsString());
    }
}
