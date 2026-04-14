package net.legitimoose.bot.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.StringUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class McUtil {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final String PLAYER_URL = "https://playerdb.co/api/player/minecraft/";

    public static String getUuidOrThrow(String username) {
        try {
            return getUuid(username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUuid(String username) throws Exception {
        return getPlayerField(username, "id");
    }

    public static String getName(String uuid) throws Exception {
        return getPlayerField(uuid, "username");
    }

    private static String getPlayerField(String usernameOrUuid, String fieldToGet) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(PLAYER_URL + usernameOrUuid))
                .GET()
                .build();

        String response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();

        JsonObject player = JsonParser.parseString(response).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("player");

        return player.get(fieldToGet).getAsString();
    }

    public static boolean isValidMCUsername(String username) {
        if (username.length() > 16)
            return false;
        for (int i = 0; i < username.length(); i++) {
            if (!isValidUsernameCharacter(username.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidUsernameCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    public static String sanitizeChat(String string) {
        return sanitizeString(string, 256);
    }

    /**
     * Replaces disallowed characters with question marks but does not trim
     */
    public static String sanitizeString(String string) {
        if (string.isEmpty())
            return string;

        StringBuilder sb = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!StringUtil.isAllowedChatCharacter(c)) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Replaces disallowed characters with question marks and trims
     */
    private static String sanitizeString(String string, int maxLength) {
        if (string.length() <= maxLength)
            return sanitizeString(string);
        return sanitizeString(string.substring(0, maxLength - 3)) + "...";
    }

}
