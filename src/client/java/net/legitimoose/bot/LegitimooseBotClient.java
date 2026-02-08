package net.legitimoose.bot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.discord.DiscordBot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class LegitimooseBotClient implements ClientModInitializer {
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
                                                Scraper.getInstance().scrape();
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
                    Scraper.getInstance().scrape();
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
                    Minecraft.getInstance().player
                            .connection
                                    .
                            sendChat("<br><red>I am a bot that syncs lobby chat to a community Discord<br>To prevent messages being sent to discord, prefix your messages with <u>::<br><reset>You can check out our work at <b>https://legiti.dev/");
                    TimeUnit.MINUTES.sleep(20);
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }).start();

        ExecutorService chatEventExecutor = Executors.newSingleThreadExecutor();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            chatEventExecutor.execute(() -> EventHandler.getInstance().onRecieveMessage(message, overlay));
        });
    }

    public static void rejoin(boolean force) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DisconnectedScreen ||
                screen instanceof JoinMultiplayerScreen ||
                screen instanceof TitleScreen ||
                screen instanceof AccessibilityOnboardingScreen ||
                (Minecraft.getInstance().getConnection() != null && force)
        ) {
            Minecraft.getInstance().schedule(() -> {
                long now = System.currentTimeMillis();
                if (now - lastJoinTimestamp >= REJOIN_COOLDOWN_MS) {
                    lastJoinTimestamp = now;
                    LOGGER.info("Attempting to reconnect to server");
                    ServerData info = new ServerData("Server", "legitimoose.com", ServerData.Type.OTHER);
                    ConnectScreen.startConnecting(
                            new JoinMultiplayerScreen(null),
                            Minecraft.getInstance(),
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
