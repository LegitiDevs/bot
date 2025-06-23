import java.util.concurrent.TimeUnit
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBot.logger
import net.legitimoose.bot.mixin.client.ChatScreenAccessor
import net.legitimoose.bot.mixin.client.CommandSuggestionsAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.multiplayer.PlayerInfo

class ListCommand(override val event: SlashCommandInteractionEvent, val lobby: Boolean?) : Command {
        override fun onCommandReceived() {
                if (lobby != null && lobby) {
                        val playerList: MutableCollection<PlayerInfo>? =
                                Minecraft.getInstance().connection?.onlinePlayers
                        val players = StringBuilder()
                        for (player in playerList!!) {
                                players.append(player.tabListDisplayName!!.string).append('\n')
                        }
                        event.reply(players.toString()).queue()
                } else {
                        event.deferReply().queue()
                        Minecraft.getInstance().execute {
                                Minecraft.getInstance().setScreen(ChatScreen("/find "))
                        }

                        try {
                                TimeUnit.SECONDS.sleep(1)
                        } catch (e: InterruptedException) {
                                logger.warn(e.message)
                        }

                        if (Minecraft.getInstance().screen is ChatScreen) {
                                val commandSuggestions =
                                        (Minecraft.getInstance().screen as ChatScreenAccessor)
                                                .commandSuggestions
                                commandSuggestions.showSuggestions(false)

                                try {
                                        TimeUnit.SECONDS.sleep(1)
                                } catch (e: InterruptedException) {
                                        logger.warn(e.message)
                                }

                                val suggestions = StringBuilder()
                                for (suggestion in
                                        (commandSuggestions as CommandSuggestionsAccessor)
                                                .getPendingSuggestions()
                                                .get()
                                                .list) {
                                        suggestions.append(suggestion.text + '\n')
                                }

                                event.hook.sendMessage(suggestions.toString()).queue()
                        } else {
                                event.hook.sendMessage("it didn't work :shrug:").queue()
                        }
                }
        }
}
