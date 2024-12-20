package me.omrih.legitimooseBot.client;

import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import me.omrih.legitimooseBot.client.command.ScrapeCommand;
import me.omrih.legitimooseBot.client.config.LegitimooseBotConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegitimooseBotClient implements ClientModInitializer {
    public static final LegitimooseBotConfig CONFIG = LegitimooseBotConfig.createAndLoad();
    public static final Logger LOGGER = Logger.getLogger("Legitimoose-Bot");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> ScrapeCommand.registerCommand(dispatcher));

        ScreenEvents.AFTER_INIT.register((minecraftClient, screen, i, i1) -> {
            if (screen instanceof TitleScreen) {
                ServerInfo info = new ServerInfo("Server", "legitimoose.com", ServerInfo.ServerType.OTHER);
                ConnectScreen.connect(new MultiplayerScreen(null), MinecraftClient.getInstance(), ServerAddress.parse("legitimoose.com"), info, false, null);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((phase, listener, client) -> {
            new Thread(() -> {
                try {
                    // wait 5 seconds to not make legmos thing that we are DDoS'ing
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    LOGGER.warning(e.getMessage());
                }

                while (true) {
                    Scraper.scrapeAll();
                    try {
                        TimeUnit.MINUTES.sleep(CONFIG.waitMinutesBetweenScrapes());
                    } catch (InterruptedException e) {
                        LOGGER.warning(e.getMessage());
                    }
                }
            }).start();
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    LOGGER.warning(e.getMessage());
                }

                while (true) {
                    MinecraftClient.getInstance().player.networkHandler.sendChatMessage("I am a bot that syncs lobby chat to discord. Prefix your message with \"::\", and I won't send it in discord.");
                    try {
                        TimeUnit.MINUTES.sleep(5);
                    } catch (InterruptedException e) {
                        LOGGER.warning(e.getMessage());
                    }
                }
            }).start();
            new Thread(DiscordMessageListener::main).start();
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
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

                    if (username.equals("Legitimooseapi")) return;

                    if (!username.isEmpty() && !cleanMessage.startsWith(CONFIG.secretPrefix())) {
                        DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
                        webhook.setUsername(username);
                        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + username);

                        EmbedObject embed = new EmbedObject().setDescription(cleanMessage);
                        if (isJoinMessage) {
                            embed.setColor(Color.GREEN); // Green color for join messages
                        }

                        webhook.getEmbeds().add(embed);
                        webhook.execute();
                    }
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }).start();
        });
    }
}