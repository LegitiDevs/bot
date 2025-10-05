package net.legitimoose.bot.discord.staff

import kotlin.system.exitProcess
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.discord.Command

class Restart(override val event: SlashCommandInteractionEvent) : Command {
  override fun onCommandReceived() {
    event.deferReply(true).queue()
    event.hook.sendMessage("Restarting bot...").complete()
    exitProcess(0)
  }
}
