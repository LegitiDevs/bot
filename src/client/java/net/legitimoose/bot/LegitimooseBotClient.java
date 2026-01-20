package net.legitimoose.bot;

import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.discord.DiscordWebhook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class LegitimooseBotClient implements ClientModInitializer {
    public static final Minecraft mc = Minecraft.getInstance();
    public static final Scraper scraper = new Scraper();

    public static List<String> lastMessages = new ArrayList<>();

    private final Pattern joinPattern = Pattern.compile("^\\[\\+]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern switchPattern = Pattern.compile("^\\[→]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern leavePattern = Pattern.compile("^\\[-]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern banPattern = Pattern.compile("^(\\S+)\\s+banned\\s+(\\S+)\\s+for\\s+'(.+)'");
    private final Pattern broadcastPattern = Pattern.compile("^\\[Broadcast\\]\\s(.*)");

    private final Pattern chatPattern = Pattern.compile("^(?:\\[SHOUT]\\s*)?(?:[^|]+\\|\\s*)?([^:]+): (.*)");
    private final Pattern msgPattern = Pattern.compile("\\[(.*) -> me] @(.*) (.*)");

    static volatile private long lastJoinTimestamp = 0L;
    private static final long REJOIN_COOLDOWN_MS = 5_000L;

    Timer timer = new Timer();

    @Override
    public void onInitializeClient() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(67);
            }
        }, TimeUnit.HOURS.toMillis(24), TimeUnit.HOURS.toMillis(24));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) -> {
            dispatcher.register(
                    ClientCommandManager.literal("scraper")
                            .then(ClientCommandManager.literal("scrape")
                                    .executes((context -> {
                                        new Thread(() -> {
                                            try {
                                                scraper.scrape();
                                            } catch (IOException | URISyntaxException ignored) {
                                            }
                                        }).start();
                                        return 1;
                                    })))
                            .then(ClientCommandManager.literal("reload")
                                    .executes((context -> {
                                        try {
                                            CONFIG.loadConfig();
                                        } catch (IOException e) {
                                            LOGGER.error(e.getMessage());
                                        }
                                        return 1;
                                    })))
            );
        });

        new Thread(DiscordBot::run).start();

        ClientTickEvents.END_CLIENT_TICK.register((minecraft) -> rejoin(false));

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
            }
            rejoin(false);
        }).start();

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
            }
            while (true) {
                try {
                    scraper.scrape();
                } catch (IOException | URISyntaxException ignored) {
                }
                try {
                    TimeUnit.MINUTES.sleep(CONFIG.getOrDefault("waitMinutesBetweenScrapes", 20));
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
                    mc.player
                            .connection
                                    .
                            sendChat("<br><red>I am a bot that syncs lobby chat to a community Discord<br>To prevent messages being sent to discord, prefix your messages with <u>::<br><reset>You can check out our work at <b>https://legiti.dev/");
                    TimeUnit.MINUTES.sleep(20);
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }).start();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            lastMessages.add(message.getString());
            new Thread(() -> {
                String msg = message.getString();
                String username = "";
                String cleanMessage = msg;

                Matcher joinMatcher = joinPattern.matcher(msg);
                Matcher switchMatcher = switchPattern.matcher(msg);
                Matcher leaveMatcher = leavePattern.matcher(msg);
                Matcher chatMatcher = chatPattern.matcher(msg);
                Matcher msgMatcher = msgPattern.matcher(msg);
                Matcher banMatcher = banPattern.matcher(msg);
                Matcher broadcastMatcher = broadcastPattern.matcher(msg);

                DiscordWebhook webhook = new DiscordWebhook(CONFIG.getOrDefault("webhookUrl", ""));
                if (joinMatcher.find()) {
                    username = joinMatcher.group(1);
                    cleanMessage = String.format("**%s** joined the server.", username);
                    webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
                    webhook.setContent(cleanMessage.replace("@", ""));
                    try {
                        webhook.execute(0x57F287);
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.warn(e.getMessage());
                    }
                    return;
                } else if (switchMatcher.find()) {
                    username = switchMatcher.group(1);
                    cleanMessage = String.format("**%s** switched servers.", username);
                    webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
                    webhook.setContent(cleanMessage.replace("@", ""));
                    try {
                        webhook.execute(0xF2F257);
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.warn(e.getMessage());
                    }
                    return;
                } else if (leaveMatcher.find()) {
                    username = leaveMatcher.group(1);
                    cleanMessage = String.format("**%s** left the server.", username);
                    webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
                    webhook.setContent(cleanMessage.replace("@", ""));
                    try {
                        webhook.execute(0xF25757);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                } else if (chatMatcher.find()) {
                    username = chatMatcher.group(1);
                    cleanMessage = chatMatcher.group(2);
                    if (msg.startsWith("[SHOUT]")) {
                        webhook.setUsername(String.format("[SHOUT] %s", username));
                    } else {
                        webhook.setUsername(username);
                    }
                    webhook.setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", username));
                } else if (msgMatcher.find()) {
                    String username1 = msgMatcher.group(1);
                    String username2 = msgMatcher.group(2);
                    String msg1 = msgMatcher.group(3);
                    Member member =
                            DiscordBot.jda
                                    .getGuildById(CONFIG.getOrDefault("discordGuildId", "1311574348989071440"))
                                    .findMembers(s -> s.getUser().getName().equals(username2))
                                    .get().getFirst();
                    member.getUser()
                            .openPrivateChannel()
                            .flatMap(channel -> channel.sendMessage(String.format("%s: %s", username1, msg1)))
                            .queue();
                    return;
                } else if (banMatcher.find()) {
                    // TODO: Add Tempban Code
                    String username1 = banMatcher.group(1);
                    String username2 = banMatcher.group(2);
                    String reason = banMatcher.group(3);
                    webhook.setContent(String.format("**%s** was banned by **%s**\nReason: %s", username2, username1, reason));
                    webhook.setUsername("Legitimoose Ban Messages");
                    try {
                        webhook.execute(0xFF0000);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                } else if (broadcastMatcher.find()) {
                    String msg1 = broadcastMatcher.group(1);
                    webhook.setUsername("[Broadcast]");
                    webhook.setContent(msg1);
                    try {
                        webhook.execute(0x5757F2);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                if (username.equals(mc.player.getName().getString())) return;

                if (!username.isEmpty() &&
                        !cleanMessage.startsWith(CONFIG.getOrDefault("secretPrefix", "::"))
                ) {
                    webhook.setContent(cleanMessage.replace("@", ""));
                    try {
                        webhook.execute();
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.warn(e.getMessage());
                    }
                }
            }).start();
        });
    }

    public static void rejoin(boolean force) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        Screen screen = mc.screen;
        if (screen instanceof DisconnectedScreen ||
                screen instanceof JoinMultiplayerScreen ||
                screen instanceof TitleScreen ||
                screen instanceof AccessibilityOnboardingScreen ||
                (mc.getConnection() != null && force)
        ) {
            Minecraft.getInstance().schedule(() -> {
                long now = System.currentTimeMillis();
                if (now - lastJoinTimestamp >= REJOIN_COOLDOWN_MS) {
                    lastJoinTimestamp = now;
                    LOGGER.info("Attempting to reconnect to server");
                    ServerData info = new ServerData("Server", "legitimoose.com", ServerData.Type.OTHER);
                    ConnectScreen.startConnecting(
                            new JoinMultiplayerScreen(null),
                            mc,
                            ServerAddress.parseString("legitimoose.com"),
                            info,
                            false,
                            null
                    );
                }
            });
        }
    }
}
