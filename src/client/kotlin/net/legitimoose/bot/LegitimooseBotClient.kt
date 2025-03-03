package net.legitimoose.bot

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.legitimoose.bot.LegitimooseBot.config
import net.legitimoose.bot.LegitimooseBot.logger
import net.legitimoose.bot.discord.DiscordBot
import net.legitimoose.bot.discord.DiscordWebhook
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.network.ServerAddress
import net.minecraft.client.network.ServerInfo
import net.minecraft.text.Text
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.thread

object LegitimooseBotClient {
    private val joinPattern: Pattern = Pattern.compile("^\\[\\+]\\s*(?:[^|]+\\|\\s*)?(\\S+)")
    private val chatPattern: Pattern = Pattern.compile("^(?:[^|]+\\|\\s*)?([^:]+):")
    private val msgPattern: Pattern = Pattern.compile("\\[(.*) -> me\\] @(.*) (.*)")

    fun init() {
        thread { DiscordBot.runBot() }

        ScreenEvents.AFTER_INIT.register { _: MinecraftClient, screen: Screen, _: Int, _: Int ->
            if (screen is TitleScreen) {
                val info = ServerInfo("Server", "legitimoose.com", ServerInfo.ServerType.OTHER)
                ConnectScreen.connect(
                    MultiplayerScreen(null),
                    MinecraftClient.getInstance(),
                    ServerAddress.parse("legitimoose.com"),
                    info,
                    false,
                    null
                )
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
                    Scraper.scrape()
                } catch (_: Exception) {
                }
                try {
                    TimeUnit.MINUTES.sleep(config.waitMinutesBetweenScrapes)
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
                    MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("lc <br><red>I am a bot that syncs lobby chat to a community Discord")
                    MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("lc <br><red>If you wish to not have your messages sent to discord, prefix your messages with <u>::</u>")
                    MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("lc You can check out the API at <bold>https://legitimoose.net/api</bold>")
                    TimeUnit.MINUTES.sleep(5)
                } catch (e: InterruptedException) {
                    logger.warn(e.message)
                }
            }
        }

        ClientReceiveMessageEvents.GAME.register { message: Text, _: Boolean ->
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
                    cleanMessage = "**$username Joined the server**"
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
                        .findMembers { s -> s.user.name == username2 }.get()[0]
                    member.user.openPrivateChannel()
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
