package net.legitimoose.bot.http.endpoint;

import jdk.jfr.Event;
import net.legitimoose.bot.EventHandler;
import net.minecraft.client.Minecraft;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class PlayersEndpoint {
    private final Pattern glistPattern = Pattern.compile("\\[(.*)] \\(\\d*\\): (.*)");

    public JSONArray handleRequest() {
        JSONArray response = new JSONArray();
        List<String> glist = getGlist();
        for (String worldMessage : glist) {
            Matcher matcher = glistPattern.matcher(worldMessage);
            if (!matcher.matches()) continue;
            JSONObject world = new JSONObject();

            String[] usernames = matcher.group(2).split(", ", -1);
            world.put("world", matcher.group(1));
            world.put("players", usernames);
            response.put(world);
        }

        return response;
    }

    public JSONObject handleRequest(String uuid) {
        JSONObject response = new JSONObject();
        List<String> glist = getGlist(uuid);
        for (String worldMessage : glist) {
            Matcher matcher = glistPattern.matcher(worldMessage);
            if (!matcher.matches()) continue;

            String[] usernames = matcher.group(2).split(", ", -1);
            response.put("players", usernames);
        }

        return response;
    }

    public List<String> getListall() {
        // Get /listall and output
        EventHandler.getInstance().lastMessages.clear();
        Minecraft.getInstance().player.connection.sendCommand("listall");
        EventHandler.getInstance().handleChat = false;
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            EventHandler.getInstance().handleChat = true;
        }
        EventHandler.getInstance().handleChat = true;
        return EventHandler.getInstance().lastMessages;
    }

    public List<String> getGlist() {
        // Get /glist all and output
        EventHandler.getInstance().lastMessages.clear();
        Minecraft.getInstance().player.connection.sendCommand("glist all");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        while (EventHandler.getInstance().lastMessages.getLast().startsWith("["));
        return EventHandler.getInstance().lastMessages;
    }

    public List<String> getGlist(String uuid) {
        // Get /glist all and output
        EventHandler.getInstance().lastMessages.clear();
        Minecraft.getInstance().player.connection.sendCommand(String.format("glist %s", uuid));
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        return EventHandler.getInstance().lastMessages;
    }
}
