package net.legitimoose.bot.discord.staff

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBotClient
import net.legitimoose.bot.discord.Command

class Send(override val event: SlashCommandInteractionEvent, val message: String) : Command {
  override fun onCommandReceived() {
    event.deferReply(true).queue()
    if (message.isEmpty()) {
      event.hook.sendMessage("Please provide a message to send.").queue()
      return
    }
    LegitimooseBotClient.mc.schedule {
      if (message.startsWith("/")) {
        LegitimooseBotClient.mc.connection?.sendCommand(message)
        return@schedule
      }
      LegitimooseBotClient.mc.connection?.sendChat(message)
      event.hook.sendMessage("Message sent: `$message`").queue()
    }
  }
}
