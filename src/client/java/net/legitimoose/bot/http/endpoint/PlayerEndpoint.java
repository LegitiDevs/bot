package net.legitimoose.bot.http.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Rank;
import net.legitimoose.bot.scraper.Scraper;
import net.legitimoose.bot.util.McUtil;

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

        for (String user : usernames.keySet()) {
            JsonObject player = new JsonObject();
            try {
                String uuid = McUtil.getUuid(user);
                Player dbPlayer = players.find(eq("uuid", uuid)).first();
                Rank rank;
                if (dbPlayer == null) rank = Rank.Unknown;
                else rank = dbPlayer.rank();

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
        List<String> glist = new PlayersEndpoint().getGlist();
        Map<String, String> usernames = new HashMap<>();
        for (String worldMessage : glist) {
            Matcher matcher = glistPattern.matcher(worldMessage);
            if (!matcher.matches()) continue;
            for (String user : matcher.group(2).split(", ", -1)) {
                usernames.put(user, matcher.group(1));
            }
        }

        for (String user : usernames.keySet()) {
            try {
                if (!McUtil.getUuid(user).equals(uuid)) {
                    continue;
                }
                Player dbPlayer = players.find(eq("uuid", uuid)).first();
                Rank rank;
                if (dbPlayer == null) rank = Rank.Unknown;
                else rank = dbPlayer.rank();

                response.addProperty("name", user);
                response.addProperty("rank", rank.toString());
                response.addProperty("world", usernames.get(user));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }
}
