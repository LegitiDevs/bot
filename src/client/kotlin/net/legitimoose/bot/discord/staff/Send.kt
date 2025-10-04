import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBotClient

class Send(override val event: SlashCommandInteractionEvent, val message: String) : Command {
  override fun onCommandReceived() {
    if (message.isEmpty()) {
      event.hook.sendMessage("Please provide a message to send.").queue()
      return
    }
    LegitimooseBotClient.mc.schedule {
      LegitimooseBotClient.mc.connection?.sendChat(message)
      event.hook.sendMessage("Message sent: `$message`").queue()
    }
  }
}
