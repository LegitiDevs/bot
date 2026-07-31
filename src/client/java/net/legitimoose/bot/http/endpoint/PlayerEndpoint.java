package net.legitimoose.bot.http.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.legitimoose.bot.scraper.Database;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Rank;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class PlayerEndpoint {
    private final Pattern listallPattern = Pattern.compile("\\[(.*)] \\(\\d*\\): (.*)");

    public JsonArray handleRequest() {
        JsonArray response = new JsonArray();
        Map<String, String> usernames = getPlayers();

        for (String user : usernames.keySet()) {
            JsonObject player = new JsonObject();
            try {
                String uuid = McUtil.getUuid(user);
                Player dbPlayer = Database.getPlayers().find(eq("uuid", uuid)).first();
                Rank rank;
                if (dbPlayer == null) {
                    rank = Rank.Unknown;
                } else {
                    player.addProperty("streak", dbPlayer.streak().days());
                    rank = dbPlayer.rank();
                }

                player.addProperty("uuid", uuid);
                player.addProperty("name", user);
                player.addProperty("rank", rank.toString());
                player.addProperty("world", usernames.get(user));
                response.add(player);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }

    public JsonObject handleRequest(String uuid) {
        JsonObject response = new JsonObject();
        Map<String, String> usernames = getPlayers();

        for (String user : usernames.keySet()) {
            try {
                if (!McUtil.getUuid(user).equals(uuid)) {
                    continue;
                }
                Player dbPlayer = Database.getPlayers().find(eq("uuid", uuid)).first();
                Rank rank;
                if (dbPlayer == null) {
                    rank = Rank.Unknown;
                } else {
                    response.addProperty("streak", dbPlayer.streak().days());
                    rank = dbPlayer.rank();
                }

                response.addProperty("name", user);
                response.addProperty("rank", rank.toString());
                response.addProperty("world", usernames.get(user));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }

    /**
     * @return A map of the player's username, and world uuid
     */
    private @NonNull Map<String, String> getPlayers() {
        List<Component> glist = new PlayersEndpoint().getListall();
        Map<String, String> usernames = new HashMap<>();
        for (Component worldMessage : glist) {
            String worldString = worldMessage.getString();
            Matcher matcher = listallPattern.matcher(worldString);
            if (!matcher.matches()) continue;
            String world;
            String worldCommand = ((ClickEvent.SuggestCommand) worldMessage.getStyle().getClickEvent()).command();
            if (worldCommand.equals("/lobby")) {
                world = "lobby";
            } else world = worldCommand.substring(7);
            for (String user : matcher.group(2).split(", ", -1)) {
                usernames.put(user, world);
            }
        }
        return usernames;
    }
}
