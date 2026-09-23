package net.legitimoose.bot.chat.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.minecraft.client.Minecraft;

public class InfoCommand {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("info").executes(context -> {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI("https://api.legiti.dev/"))
                                .GET()
                                .build();
                        String response = client.send(request, HttpResponse.BodyHandlers.ofString())
                                .body();
                        JsonObject info = JsonParser.parseString(response).getAsJsonObject();
                        int ping = Minecraft.getInstance()
                                .getConnection()
                                .getPlayerInfo(Minecraft.getInstance().player.getUUID())
                                .getLatency();

                        context.getSource()
                                .sendMessage(String.format(
                                        "Ping: %sms<br>API Version: %s<br>Bot Version: %s",
                                        ping,
                                        info.get("version").getAsString(),
                                        info.getAsJsonObject("scraper")
                                                .get("version")
                                                .getAsString()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
