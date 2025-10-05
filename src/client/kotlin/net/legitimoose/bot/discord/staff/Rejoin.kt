package net.legitimoose.bot.discord.staff

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBotClient
import net.legitimoose.bot.discord.Command

class Rejoin(override val event: SlashCommandInteractionEvent) : Command {
  override fun onCommandReceived() {
    event.deferReply(true).queue()
    event.hook.sendMessage("Rejoining server...").queue()
    LegitimooseBotClient.rejoin(force = true)
  }
}
