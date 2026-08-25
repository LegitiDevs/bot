package net.legitimoose.bot.http.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.legitimoose.bot.scraper.Database;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Rank;
import net.legitimoose.bot.util.McUtil;
import org.bson.Document;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
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

        for (String username : usernames.keySet()) {
            try {
                if (Database.getPlayers().countDocuments(new Document("name", username)) == 0) {
                    new Player(McUtil.getUuid(username), username, Rank.Unknown, List.of(), new Player.Streak(1, false), Instant.EPOCH, 0).write();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (Player dbPlayer : Database.getPlayers().find()) {
            JsonObject player = new JsonObject();
            try {
                Rank rank;
                if (dbPlayer == null) {
                    rank = Rank.Unknown;
                } else {
                    player.addProperty("streak", dbPlayer.streak().days());
                    player.addProperty("legiticoins", dbPlayer.legiticoins());
                    rank = dbPlayer.rank();
                }

                boolean online = false;
                String world = "";
                if (usernames.get(dbPlayer.name()) != null) {
                    online = true;
                    world = usernames.get(dbPlayer.name());
                }

                player.addProperty("uuid", dbPlayer.uuid());
                player.addProperty("name", dbPlayer.name());
                player.addProperty("rank", rank.toString());
                player.addProperty("online", online);
                player.addProperty("world", world);
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

        for (String username : usernames.keySet()) {
            try {
                if (Database.getPlayers().countDocuments(new Document("name", username)) == 0) {
                    new Player(McUtil.getUuid(username), username, Rank.Unknown, List.of(), new Player.Streak(1, false), Instant.EPOCH, 0).write();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Player dbPlayer = Database.getPlayers().find(eq("uuid", uuid)).first();
        try {
            boolean online = false;
            String world = "";
            if (usernames.get(dbPlayer.name()) != null) {
                online = true;
                world = usernames.get(dbPlayer.name());
            }

            response.addProperty("uuid", dbPlayer.uuid());
            response.addProperty("name", dbPlayer.name());
            response.addProperty("rank", dbPlayer.rank().toString());
            response.addProperty("streak", dbPlayer.streak().days());
            response.addProperty("online", online);
            response.addProperty("world", world);
            response.addProperty("legiticoins", dbPlayer.legiticoins());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
