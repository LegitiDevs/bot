package me.omrih.legitimooseBot.client;

import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import me.micartey.webhookly.embeds.Footer;
import me.omrih.legitimooseBot.client.command.ScrapeCommand;
import me.omrih.legitimooseBot.client.config.LegitimooseBotConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegitimooseBotClient implements ClientModInitializer {
    public static final LegitimooseBotConfig CONFIG = LegitimooseBotConfig.createAndLoad();
    public static final Logger LOGGER = Logger.getLogger("Legitimoose-Bot");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> ScrapeCommand.registerCommand(dispatcher));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            SimpleDateFormat dateFormatUtc = new SimpleDateFormat("HH:mm:ss");
            dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
            final Pattern JOIN_PATTERN = Pattern.compile("^\\[\\+]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
            final Pattern CHAT_PATTERN = Pattern.compile("^(?:[^|]+\\|\\s*)?([^:]+):");
            new Thread(() -> {
                try {
                    String msg = message.getString();
                    String username = "";
                    String cleanMessage = msg;
                    boolean isJoinMessage = false;

                    Matcher joinMatcher = JOIN_PATTERN.matcher(msg);
                    Matcher chatMatcher = CHAT_PATTERN.matcher(msg);

                    if (joinMatcher.find()) {
                        username = joinMatcher.group(1);
                        cleanMessage = "**" + username + " joined the server**";
                        isJoinMessage = true;
                    } else if (chatMatcher.find()) {
                        username = chatMatcher.group(1);
                        cleanMessage = msg.substring(chatMatcher.end()).trim();
                    }

                    if (!username.isEmpty()) {
                        DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
                        webhook.setUsername(username);
                        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + username);

                        EmbedObject embed = new EmbedObject().setDescription(cleanMessage).setFooter(new Footer(getCurrentUtcTime() + " UTC", ""));
                        if (isJoinMessage) {
                            embed.setColor(Color.GREEN); // Green color for join messages
                        }

                        webhook.getEmbeds().add(embed);
                        webhook.execute();
                    }
                } catch (Exception e) {
                    LOGGER.info(e.getMessage());
                }
            }).start();
        });
    }

    public static Date getCurrentUtcTime() {  // handling ParseException
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat ldf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date d1 = null;
        try {
            d1 = ldf.parse(sdf.format(new Date()));
        }
        catch (java.text.ParseException e) {
            LOGGER.info(e.getMessage());
        }
        return d1;
    }
}