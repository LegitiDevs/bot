package net.legitimoose.bot.util;

/*
 * Used to do webhook stuff easily
 * Taken from https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb
 */

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class DiscordWebhook {

    public static class Embed {
        private final String title;
        private final Integer color;
        private String description;
        private String thumbnailUrl;

        public Embed(String title, Integer color) {
            this.title = title;
            this.color = color;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setThumbnail(String imageUrl) {
            this.thumbnailUrl = imageUrl;
        }
    }

    private static final Gson GSON = new Gson();
    private final String url;
    private String content;
    private String username;
    private String avatarUrl;

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void execute() throws IOException, URISyntaxException {
        execute(null);
    }

    public void execute(Embed embed) throws IOException, URISyntaxException {
        if (this.content == null) {
            throw new IllegalArgumentException("Content must be set");
        }

        JsonObject json = new JsonObject();

        if (embed == null) {
            json.addProperty("content", this.content);
        } else {
            JsonObject embedJson = new JsonObject();
            embedJson.addProperty("title", embed.title);
            embedJson.addProperty("color", embed.color);

            if (embed.description != null) {
                embedJson.addProperty("description", embed.description);
            }
            if (embed.thumbnailUrl != null) {
                JsonObject thumbnail = new JsonObject();
                thumbnail.addProperty("url", embed.thumbnailUrl);
                embedJson.add("thumbnail", thumbnail);
            }

            JsonArray embeds = new JsonArray();
            embeds.add(embedJson);

            json.add("embeds", embeds);
        }
        json.addProperty("username", this.username);
        json.addProperty("avatar_url", this.avatarUrl);
        json.addProperty("tts", false);

        URL url = new URI(this.url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhookBuilder-LegitiDevs");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(GSON.toJson(json).getBytes());
        stream.flush();
        stream.close();

        connection
                .getInputStream()
                .close(); // I'm not sure why but it doesn't work without getting the InputStream
        connection.disconnect();
    }
}
