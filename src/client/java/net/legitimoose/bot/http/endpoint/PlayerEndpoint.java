package net.legitimoose.bot.http.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Rank;
import net.legitimoose.bot.scraper.Scraper;
import net.legitimoose.bot.util.McUtil;
import org.bson.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class PlayerEndpoint {
    private final Pattern glistPattern = Pattern.compile("\\[(.*)] \\(\\d*\\): (.*)");
    private final MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);

    public JsonArray handleRequest() {
        JsonArray response = new JsonArray();
        List<String> glist = new PlayersEndpoint().getGlist();
        Map<String, String> usernames = new HashMap<>();
        for (String worldMessage : glist) {
            Matcher matcher = glistPattern.matcher(worldMessage);
            if (!matcher.matches()) continue;
            for (String user : matcher.group(2).split(", ", -1)) {
                usernames.put(user, matcher.group(1));
            }
        }

        for (String username : usernames.keySet()) {
            try {
                if (players.countDocuments(new Document("name", username)) == 0) {
                    new Player(McUtil.getUuid(username), username, Rank.Unknown, List.of()).write();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (Player dbPlayer : players.find()) {
            JsonObject player = new JsonObject();
            try {
                Rank rank;
                if (dbPlayer == null) rank = Rank.Unknown;
                else rank = dbPlayer.rank();

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
        List<String> glist = new PlayersEndpoint().getGlist();
        Map<String, String> usernames = new HashMap<>();
        for (String worldMessage : glist) {
            Matcher matcher = glistPattern.matcher(worldMessage);
            if (!matcher.matches()) continue;
            for (String user : matcher.group(2).split(", ", -1)) {
                usernames.put(user, matcher.group(1));
            }
        }

        for (String username : usernames.keySet()) {
            try {
                if (players.countDocuments(new Document("name", username)) == 0) {
                    new Player(McUtil.getUuid(username), username, Rank.Unknown, List.of()).write();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Player dbPlayer = players.find(eq("uuid", uuid)).first();
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
            response.addProperty("online", online);
            response.addProperty("world", world);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
    }
}
