package net.legitimoose.bot.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.legitimoose.bot.LegitimooseBot.config
import net.legitimoose.bot.LegitimooseBot.logger
import net.legitimoose.bot.mixin.client.ChatInputSuggestorAccessor
import net.legitimoose.bot.mixin.client.ChatScreenAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import java.util.concurrent.TimeUnit

class DiscordBot : ListenerAdapter() {
    companion object {
        lateinit var jda: JDA
        fun runBot() {
            jda = JDABuilder.createDefault(config.discordToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS).build()

            jda.addEventListener(DiscordBot())
            jda.updateCommands().addCommands(
                Commands.slash("list", "List online players in the server")
                    .addOption(OptionType.BOOLEAN, "lobby", "True if you only want to see online players in the lobby"),
                Commands.slash("find", "Find which world a player is in")
                    .addOption(OptionType.STRING, "player", "The username of the player you want to find", true),
                Commands.slash("msg", "Message an ingame player")
                    .addOption(OptionType.STRING, "player", "The username of the player you want to message", true)
                    .addOption(OptionType.STRING, "message", "The message you want to send", true)
            ).queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "list") {
            if (event.getOption("lobby") != null && event.getOption("lobby")!!.asBoolean) {
                val playerList: MutableCollection<PlayerListEntry>? =
                    MinecraftClient.getInstance().networkHandler?.playerList
                val players = StringBuilder()
                for (player in playerList!!) {
                    players.append(player.displayName!!.string).append('\n')
                }
                event.reply(players.toString()).queue()
            } else {
                event.deferReply().queue()
                MinecraftClient.getInstance().execute { MinecraftClient.getInstance().setScreen(ChatScreen("/find ")) }

                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    logger.warn(e.message)
                }

                if (MinecraftClient.getInstance().currentScreen is ChatScreen) {
                    val chatInputSuggestor =
                        (MinecraftClient.getInstance().currentScreen as ChatScreenAccessor).chatInputSuggestor
                    chatInputSuggestor.show(false)

                    try {
                        TimeUnit.SECONDS.sleep(1)
                    } catch (e: InterruptedException) {
                        logger.warn(e.message)
                    }

                    val suggestions = StringBuilder()
                    for (suggestion in (chatInputSuggestor as ChatInputSuggestorAccessor).getPendingSuggestions()
                        .get().list) {
                        suggestions.append(suggestion.text + '\n')
                    }

                    event.hook.sendMessage(suggestions.toString()).queue()

                } else {
                    event.hook.sendMessage("it didn't work :shrug:").queue()
                }
            }
        } else if (event.name == "find") {
            MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand(
                "find " + event.getOption("player")!!.asString
            )
            val bool: BooleanArray = booleanArrayOf(true)
            ClientReceiveMessageEvents.GAME.register { message: Text, _: Boolean ->
                if (!bool[0]) return@register
                event.reply(message.string.replace(" Click HERE to join.", "").trim()).queue()
                bool[0] = false
            }
        } else if (event.name == "msg") {
            MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand(
                "msg " + event.getOption("player")!!.asString + " [ᴅɪsᴄᴏʀᴅ] " + event.member
                !!.effectiveName + ": " + event.getOption("message")!!.asString.replace("\n", "<br>")
                    .replace("§", "?")
            )
            event.reply(
                "Sent `" + event.getOption("message")!!.asString.trim() + "` to " + event.getOption("player")
                !!.asString
            ).setEphemeral(true).queue()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isWebhookMessage || event.author.isBot) return
        val discordNick = event.member!!.effectiveName.replace("§", "?")
        val message =
            "<br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>$discordNick</yellow><dark_gray>:</dark_gray> " + event.message.contentStripped.replace(
                "\n",
                "<br>"
            ).replace("§", "?")
        if (message.length >= 200) return
        if (event.channel.id == config.channelId) {
            MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("lc $message")
        }
    }
}
