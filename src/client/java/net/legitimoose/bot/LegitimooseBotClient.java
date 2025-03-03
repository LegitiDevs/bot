package net.legitimoose.bot;

import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.discord.DiscordWebhook;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class LegitimooseBotClient implements ClientModInitializer {
    private static final Pattern JOIN_PATTERN = Pattern.compile("^\\[\\+]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private static final Pattern CHAT_PATTERN = Pattern.compile("^(?:[^|]+\\|\\s*)?([^:]+):");
    private static final Pattern MSG_PATTERN = Pattern.compile("\\[(.*) -> me\\] @(.*) (.*)");

    @Override
    public void onInitializeClient() {
        new Thread(DiscordBot::runBot).start();

        ScreenEvents.AFTER_INIT.register((minecraftClient, screen, i, i1) -> {
            if (screen instanceof TitleScreen) {
                ServerInfo info = new ServerInfo("Server", "legitimoose.com", ServerInfo.ServerType.OTHER);
                ConnectScreen.connect(new MultiplayerScreen(null), MinecraftClient.getInstance(), ServerAddress.parse("legitimoose.com"), info, false, null);
            }
        });

        new Thread(() -> {
            try {
                // wait 5 seconds to not make legmos thing that we are DDoS'ing
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
            }

            while (true) {
                try {
                    Scraper.scrapeAll();
                } catch (Exception ignored) {
                }
                try {
                    TimeUnit.MINUTES.sleep(CONFIG.waitMinutesBetweenScrapes());
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }).start();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
            }

            while (true) {
                try {
                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc <br><red>I am a bot that syncs lobby chat to a community Discord");
                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc <br><red>If you wish to not have your messages sent to discord, prefix your messages with <u>::</u>");
                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc You can check out the API at <bold>https://legitimoose.net/api</bold>");
                    TimeUnit.MINUTES.sleep(5);
                } catch (Exception ignored) {
                }
                try {
                    TimeUnit.MINUTES.sleep(15);
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }).start();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            new Thread(() -> {
                try {
                    String msg = message.getString();
                    String username = "";
                    String cleanMessage = msg;

                    Matcher joinMatcher = JOIN_PATTERN.matcher(msg);
                    Matcher chatMatcher = CHAT_PATTERN.matcher(msg);
                    Matcher msgMatcher = MSG_PATTERN.matcher(msg);

                    DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
                    if (joinMatcher.find()) {
                        username = joinMatcher.group(1);
                        cleanMessage = "**" + username + " Joined the server**";
                    } else if (chatMatcher.find()) {
                        username = chatMatcher.group(1);
                        cleanMessage = msg.substring(chatMatcher.end()).trim();
                        webhook.setUsername(username);
                        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + username);
                    } else if (msgMatcher.find()) {
                        String username1 = msgMatcher.group(1);
                        String username2 = msgMatcher.group(2);
                        String msg1 = msgMatcher.group(3);
                        Member member = DiscordBot.jda.getGuildById(1311574348989071440L).findMembers(s -> s.getUser().getName().equals(username2)).get().getFirst();
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(String.format("%s: %s", username1, msg1))).queue();
                        return;
                    }

                    if (username.equals("Legitimooseapi")) return;

                    if (!username.isEmpty() && !cleanMessage.startsWith(CONFIG.secretPrefix())) {
                        webhook.setContent(cleanMessage.replace("@", ""));
                        webhook.execute();
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }).start();
        });
    }
}
