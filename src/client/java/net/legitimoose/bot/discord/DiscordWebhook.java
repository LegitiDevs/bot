package net.legitimoose.bot.discord;

/*
 * Used to do webhook stuff easily
 * Taken from https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb
 */

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

public class DiscordWebhook {

  private static final Gson GSON = new Gson();
  private final String url;
  private String content;
  private String username;
  private String avatarUrl;
  private String embedImageUrl;

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

  public void setEmbedThumbnail(String imageUrl) {
    this.embedImageUrl = imageUrl;
  }

  public void execute() throws IOException, URISyntaxException {
    execute(null);
  }

  public void execute(Integer embedColor) throws IOException, URISyntaxException {
    if (this.content == null) {
      throw new IllegalArgumentException("Content must be set");
    }

    JsonObject json = new JsonObject();

    if (embedColor == null) {
      json.addProperty("content", this.content);
    } else {
      JsonObject embed = new JsonObject();
      embed.addProperty("title", this.content);
      embed.addProperty("color", embedColor);

      if (embedImageUrl != null) {
        JsonObject thumbnail = new JsonObject();
        thumbnail.addProperty("url", embedImageUrl);
        embed.add("thumbnail", thumbnail);
      }

      JsonArray embeds = new JsonArray();
      embeds.add(embed);

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
