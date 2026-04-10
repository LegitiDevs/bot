package net.legitimoose.bot;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.http.HttpServer;
import net.legitimoose.bot.scraper.Scraper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class LegitimooseBotClient implements ClientModInitializer {

    private static final String MESSAGE =
            "<br><red>I am a bot that syncs lobby chat to a community Discord<br>" +
            "To prevent messages being sent to discord, prefix your messages with <u>::<br>" +
            "<reset>You can check out our work at <b>https://legiti.dev/";

    private static volatile long lastJoinTimestamp = 0L;

    private static final long REJOIN_COOLDOWN_MS = 5000L;

    private static final int TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

    private final Timer timer = new Timer();

    private static ExecutorService threadPool;

    @Override
    public void onInitializeClient() {
        scheduleExit();

        registerCommands();

        threadPool = Executors.newFixedThreadPool(4);

        threadPool.execute(DiscordBot::run);

        ClientTickEvents.END_CLIENT_TICK.register((minecraft) -> attemptRejoin(false));

        schedulePeriodicalMessage();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            threadPool.execute(() -> {
                try {
                    GameChatHandler.getInstance().onReceiveMessage(message);
                } catch (CommandSyntaxException ignored) {
                }
            });
        });

        threadPool.execute(() -> HttpServer.getInstance().start());
    }

    public static void handleLogin() {
        if (Scraper.getInstance().shouldStartScraping()) {
            threadPool.execute(() -> Scraper.getInstance().startScraping());
        }
    }

    private void scheduleExit() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(67);
            }
        }, TWENTY_FOUR_HOURS, TWENTY_FOUR_HOURS);
    }

    private void schedulePeriodicalMessage() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                player.connection.sendChat(MESSAGE);
            }
        }, 0, 20, TimeUnit.MINUTES);
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            dispatcher.register(
                ClientCommandManager.literal("scraper")
                    .then(ClientCommandManager.literal("reload")
                      .executes(LegitimooseBotClient::reloadConfig)
                    )
                    .then(ClientCommandManager.literal("on")
                       .executes((source)->{
                           Scraper.getInstance().override(false);
                           return 0;
                       })
                    )
                    .then(ClientCommandManager.literal("off")
                        .executes((source)->{
                            Scraper.getInstance().override(true);
                            return 0;
                        })
                    )
            );
        });
    }

    private static int reloadConfig(CommandContext<?> context) {
        try {
            CONFIG.reloadConfiguration();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return 1;
        }
        return 0;
    }

    public static void attemptRejoin(boolean force) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment())
            return;

        if (hasDisconnected() || (Minecraft.getInstance().getConnection() != null && force)) {
            Minecraft.getInstance().schedule(LegitimooseBotClient::rejoin);
        }
    }

    private static void rejoin() {
        long now = System.currentTimeMillis();

        if ((now - lastJoinTimestamp) >= REJOIN_COOLDOWN_MS) {
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
    }

    private static boolean hasDisconnected() {
        Screen screen = Minecraft.getInstance().screen;
        return screen instanceof DisconnectedScreen ||
               screen instanceof JoinMultiplayerScreen ||
               screen instanceof TitleScreen ||
               screen instanceof AccessibilityOnboardingScreen;
    }

    private static void message(String message) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            player.displayClientMessage(Component.literal(message), false);
        }
    }

    public static void messageFromOtherThread(String message) {
        Minecraft.getInstance().submit(()->message(message));
    }
}
