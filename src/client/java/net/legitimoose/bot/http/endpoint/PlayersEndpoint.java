package net.legitimoose.bot.http.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.legitimoose.bot.chat.GameChatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class PlayersEndpoint {
    private final Pattern listallPattern = Pattern.compile("\\[(.*)] \\(\\d*\\): (.*)");
    private final Gson gson = new Gson();

    public JsonArray handleRequest() {
        JsonArray response = new JsonArray();
        List<Component> listall = getListall();
        for (Component worldMessage : listall) {
            String worldString = worldMessage.getString();
            Matcher matcher = listallPattern.matcher(worldString);
            if (!matcher.matches()) continue;
            JsonObject world = new JsonObject();

            String[] usernames = matcher.group(2).split(", ", -1);
            JsonArray players = new JsonArray();

            for (String username : usernames) {
                players.add(username);
            }
            String uuid;
            String worldCommand = ((ClickEvent.SuggestCommand) worldMessage.getStyle().getClickEvent()).command();
            if (worldCommand.equals("/lobby")) {
                uuid = "lobby";
            } else uuid = worldCommand.substring(7);

            world.addProperty("world", uuid);
            world.add("players", players);
            response.add(world);
        }

        return response;
    }

    public JsonObject handleRequest(String uuid) {
        JsonObject response = new JsonObject();
        List<Component> glist = getListall();
        for (Component worldMessage : glist) {
            String worldString = worldMessage.getString();
            Matcher matcher = listallPattern.matcher(worldString);
            if (!matcher.matches()) continue;

            String[] usernames = matcher.group(2).split(", ", -1);
            String worldUuid;
            String worldCommand = ((ClickEvent.SuggestCommand) worldMessage.getStyle().getClickEvent()).command();
            if (worldCommand.equals("/lobby")) {
                worldUuid = "lobby";
            } else worldUuid = worldCommand.substring(7);
            if (!worldUuid.equals(uuid)) continue;

            JsonArray players = new JsonArray();
            for (String username : usernames) {
                players.add(username);
            }
            response.add("players", players);
        }

        return response;
    }

    public List<Component> getListall() {
        // Get /listall and output
        GameChatHandler.getInstance().lastMessages.clear();
        Minecraft.getInstance().player.connection.sendCommand("listall");
        GameChatHandler.getInstance().handleChat = false;
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            GameChatHandler.getInstance().handleChat = true;
        }
        GameChatHandler.getInstance().handleChat = true;
        while (GameChatHandler.getInstance().lastMessages.getLast().getString().startsWith("[")) ;
        return GameChatHandler.getInstance().lastMessages;
    }
}
