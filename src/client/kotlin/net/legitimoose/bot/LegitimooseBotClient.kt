package net.legitimoose.bot

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.thread
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.legitimoose.bot.LegitimooseBot.config
import net.legitimoose.bot.LegitimooseBot.logger
import net.legitimoose.bot.discord.DiscordBot
import net.legitimoose.bot.discord.DiscordWebhook
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.DisconnectedScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress
import net.minecraft.network.chat.Component

object LegitimooseBotClient {
    private val mc get() = Minecraft.getInstance()

    private val joinPattern: Pattern = Pattern.compile(
        """^\[\+\]\s*(?:[^|]+\|\s*)?(\S+)"""
    )
    // idk why but it was erroring so i did that lmao -hazel

    private val chatPattern: Pattern = Pattern.compile("^(?:[^|]+\\|\\s*)?([^:]+):")
    private val msgPattern: Pattern = Pattern.compile("\\[(.*) -> me\\] @(.*) (.*)")

    @Volatile
    private var lastJoinTimestamp: Long = 0L
    private const val REJOIN_COOLDOWN_MS = 5_000L

    fun init() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("scrape").executes {
                    thread { Scraper.scrape() }
                    1
                }
            )
        }

        thread { DiscordBot.runBot() }

        ClientTickEvents.END_CLIENT_TICK.register {
            val screen = mc.screen
            if (screen is DisconnectedScreen || screen is JoinMultiplayerScreen || screen is TitleScreen) {
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

        thread {
            try { TimeUnit.SECONDS.sleep(5) } catch (e: InterruptedException) { logger.warn(e.message) }
            while (true) {
                try { Scraper.scrape() } catch (_: Exception) {}
                try { TimeUnit.MINUTES.sleep(config.waitMinutesBetweenScrapes) } catch (e: InterruptedException) { logger.warn(e.message) }
            }
        }

        thread {
            try { TimeUnit.SECONDS.sleep(5) } catch (e: InterruptedException) { logger.warn(e.message) }
            while (true) {
                try {
                    mc.player?.connection?.sendCommand(
                        "lc <br><red>I am a bot that syncs lobby chat to a community Discord"
                    )
                    mc.player?.connection?.sendCommand(
                        "lc <br><red>If you wish to not have your messages sent to discord, prefix your messages with <u>::</u>"
                    )
                    mc.player?.connection?.sendCommand(
                        "lc You can check out the API at <bold>https://legitimoose.net/api</bold>"
                    )
                    TimeUnit.MINUTES.sleep(20)
                } catch (e: InterruptedException) { logger.warn(e.message) }
            }
        }

        ClientReceiveMessageEvents.GAME.register { message: Component, _: Boolean ->
            thread {
                val msg = message.string
                var username = ""
                var cleanMessage = msg

                val joinMatcher = joinPattern.matcher(msg)
                val chatMatcher = chatPattern.matcher(msg)
                val msgMatcher = msgPattern.matcher(msg)

                val webhook = DiscordWebhook(config.webhookUrl)
                if (joinMatcher.find()) {
                    username = joinMatcher.group(1)
                    cleanMessage = "**$username** joined the server."
                    webhook.setEmbedThumbnail("https://mc-heads.net/head/$username/50/left")
                    webhook.setContent(cleanMessage.replace("@", ""))
                    webhook.execute(true)
                    return@thread
                } else if (chatMatcher.find()) {
                    username = chatMatcher.group(1)
                    cleanMessage = msg.substring(chatMatcher.end()).trim()
                    webhook.setUsername(username)
                    webhook.setAvatarUrl("https://mc-heads.net/avatar/$username")
                } else if (msgMatcher.find()) {
                    val username1 = msgMatcher.group(1)
                    val username2 = msgMatcher.group(2)
                    val msg1 = msgMatcher.group(3)
                    val member = DiscordBot.jda.getGuildById(1311574348989071440L)!!
                        .findMembers { s -> s.user.name == username2 }
                        .get()[0]
                    member.user
                        .openPrivateChannel()
                        .flatMap { channel -> channel.sendMessage("$username1: $msg1") }
                        .queue()
                    return@thread
                }

                if (username == "Legitimooseapi") return@thread

                if (username.isNotEmpty() && !cleanMessage.startsWith(config.secretPrefix)) {
                    webhook.setContent(cleanMessage.replace("@", ""))
                    webhook.execute()
                }
            }
        }
    }
}
