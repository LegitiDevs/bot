package net.legitimoose.bot

import java.util.Timer
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.schedule
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.system.exitProcess
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.legitimoose.bot.LegitimooseBot.config
import net.legitimoose.bot.LegitimooseBot.logger
import net.legitimoose.bot.discord.DiscordBot
import net.legitimoose.bot.discord.DiscordWebhook
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.DisconnectedScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress
import net.minecraft.network.chat.Component

object LegitimooseBotClient {
    val mc
        get() = Minecraft.getInstance()

    private val joinPattern: Pattern = Pattern.compile("""^\[\+]\s*(?:[^|]+\|\s*)?(\S+)""")
    private val switchPattern: Pattern = Pattern.compile("""^\[→]\s*(?:[^|]+\|\s*)?(\S+)""")
    private val leavePattern: Pattern = Pattern.compile("""^\[-]\s*(?:[^|]+\|\s*)?(\S+)""")

    private val chatPattern: Pattern = Pattern.compile("""^(?:\[SHOUT]\s*)?(?:[^|]+\|\s*)?([^:]+): (.*)""")
    private val msgPattern: Pattern = Pattern.compile("""\[(.*) -> me] @(.*) (.*)""")

    @Volatile
    private var lastJoinTimestamp: Long = 0L
    private const val REJOIN_COOLDOWN_MS = 5_000L

    private val timer = Timer()

    fun init() {
        timer.schedule(TimeUnit.HOURS.toMillis(24), TimeUnit.HOURS.toMillis(24)) { exitProcess(67) }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("scrape").executes {
                    thread { Scraper.scrape() }
                    1
                })
        }

        thread { DiscordBot.runBot() }

        ClientTickEvents.END_CLIENT_TICK.register { rejoin() }

        thread {
            try {
                TimeUnit.SECONDS.sleep(10)
            } catch (e: InterruptedException) {
                logger.warn(e.message)
            }
            logger.info("Trying to join for the first time")
            rejoin()
        }

        thread {
            try {
                TimeUnit.SECONDS.sleep(5)
            } catch (e: InterruptedException) {
                logger.warn(e.message)
            }
            while (true) {
                try {
                    Scraper.scrape()
                } catch (_: Exception) {
                }
                try {
                    TimeUnit.MINUTES.sleep(config.getOrDefault("waitMinutesBetweenScrapes", 10).toLong())
                } catch (e: InterruptedException) {
                    logger.warn(e.message)
                }
            }
        }

        thread {
            try {
                TimeUnit.SECONDS.sleep(5)
            } catch (e: InterruptedException) {
                logger.warn(e.message)
            }
            while (true) {
                try {
                    mc.player
                        ?.connection
                        ?.sendChat("<br><red>I am a bot that syncs lobby chat to a community Discord<br>To prevent messages being sent to discord, prefix your messages with <u>::<br><reset>You can check out our work at <b>https://legiti.dev/")
                    TimeUnit.MINUTES.sleep(20)
                } catch (e: InterruptedException) {
                    logger.warn(e.message)
                }
            }
        }

        ClientReceiveMessageEvents.GAME.register { message: Component, _: Boolean ->
            thread {
                val msg = message.string
                var username = ""
                var cleanMessage = msg

                val joinMatcher = joinPattern.matcher(msg)
                val switchMatcher = switchPattern.matcher(msg)
                val leaveMatcher = leavePattern.matcher(msg)
                val chatMatcher = chatPattern.matcher(msg)
                val msgMatcher = msgPattern.matcher(msg)

                val webhook = DiscordWebhook(config.getOrDefault("webhookUrl", ""))
                if (joinMatcher.find()) {
                    username = joinMatcher.group(1)
                    cleanMessage = "**$username** joined the server."
                    webhook.setEmbedThumbnail("https://mc-heads.net/head/$username/50/left")
                    webhook.setContent(cleanMessage.replace("@", ""))
                    webhook.execute(0x57F287)
                    return@thread
                } 
                else if (switchMatcher.find()) {
                    username = switchMatcher.group(1)
                    cleanMessage = "**$username** switched servers."
                    webhook.setEmbedThumbnail("https://mc-heads.net/head/$username/50/left")
                    webhook.setContent(cleanMessage.replace("@", ""))
                    webhook.execute(0xF2F257)
                    return@thread
                }
                else if (leaveMatcher.find()) {
                    username = leaveMatcher.group(1)
                    cleanMessage = "**$username** left the server."
                    webhook.setEmbedThumbnail("https://mc-heads.net/head/$username/50/left")
                    webhook.setContent(cleanMessage.replace("@", ""))
                    webhook.execute(0xF25757)
                    return@thread
                }
                else if (chatMatcher.find()) {
                    username = chatMatcher.group(1)
                    cleanMessage = chatMatcher.group(2)
                    if (msg.startsWith("[SHOUT]")) {
                        webhook.setUsername("[SHOUT] $username")
                    } else {
                        webhook.setUsername(username)
                    }
                    webhook.setAvatarUrl("https://mc-heads.net/avatar/$username")
                } else if (msgMatcher.find()) {
                    val username1 = msgMatcher.group(1)
                    val username2 = msgMatcher.group(2)
                    val msg1 = msgMatcher.group(3)
                    val member =
                        DiscordBot.jda
                            .getGuildById(1311574348989071440L)!!
                            .findMembers { s -> s.user.name == username2 }
                            .get()[0]
                    member.user
                        .openPrivateChannel()
                        .flatMap { channel -> channel.sendMessage("$username1: $msg1") }
                        .queue()
                    return@thread
                }

                if (username == mc.player?.name?.string) return@thread

                if (username.isNotEmpty() &&
                    !cleanMessage.startsWith(config.getOrDefault("secretPrefix", "::"))
                ) {
                    webhook.setContent(cleanMessage.replace("@", ""))
                    webhook.execute()
                }
            }
        }
    }

    fun rejoin(force: Boolean = false) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) return
        val screen = mc.screen
        if (screen is DisconnectedScreen ||
            screen is JoinMultiplayerScreen ||
            screen is TitleScreen ||
            screen is AccessibilityOnboardingScreen ||
            (mc.connection != null && force)
        ) {
            Minecraft.getInstance().schedule {
                val now = System.currentTimeMillis()
                if (now - lastJoinTimestamp >= REJOIN_COOLDOWN_MS) {
                    lastJoinTimestamp = now
                    logger.info("Attempting to reconnect to server")
                    val info = ServerData("Server", "legitimoose.com", ServerData.Type.OTHER)
                    ConnectScreen.startConnecting(
                        JoinMultiplayerScreen(null),
                        mc,
                        ServerAddress.parseString("legitimoose.com"),
                        info,
                        false,
                        null
                    )
                }
            }
        }
    }
}
