package net.legitimoose.bot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.chat.EventHandler;
import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.http.HttpServer;
import net.legitimoose.bot.scraper.Scraper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class LegitimooseBotClient implements ClientModInitializer {
    static volatile private long lastJoinTimestamp = 0L;
    private static final long REJOIN_COOLDOWN_MS = 5_000L;

    Timer timer = new Timer();

    @Override
    public void onInitializeClient() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(67);
            }
        }, TimeUnit.HOURS.toMillis(24), TimeUnit.HOURS.toMillis(24));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) -> {
            dispatcher.register(
                    ClientCommandManager.literal("scraper")
                            .then(ClientCommandManager.literal("reload")
                                    .executes((context -> {
                                        try {
                                            CONFIG.reloadConfiguration();
                                        } catch (Exception e) {
                                            LOGGER.error(e.getMessage());
                                        }
                                        return 1;
                                    })))
            );
        });

        executor.execute(DiscordBot::run);

        ClientTickEvents.END_CLIENT_TICK.register((minecraft) -> {
            rejoin(false);
            if (!Scraper.getInstance().getScraping() && Minecraft.getInstance().player != null) executor.execute(() -> {
                Scraper.getInstance().startScraping();
            });
        });

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player
                        .connection
                        .sendChat("<br><red>I am a bot that syncs lobby chat to a community Discord<br>" +
                                "To prevent messages being sent to discord, prefix your messages with <u>::<br>" +
                                "<reset>You can check out our work at <b>https://legiti.dev/");
            }
        }, 0, 20, TimeUnit.MINUTES);

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            executor.execute(() -> {
                try {
                    EventHandler.getInstance().onRecieveMessage(message, overlay);
                } catch (CommandSyntaxException ignored) {
                }
            });
        });

        executor.execute(() -> HttpServer.getInstance().start());
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
