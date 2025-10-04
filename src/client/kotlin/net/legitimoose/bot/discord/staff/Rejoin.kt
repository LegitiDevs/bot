import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.legitimoose.bot.LegitimooseBotClient

class Rejoin(override val event: SlashCommandInteractionEvent) : Command {
  override fun onCommandReceived() {
    event.hook.sendMessage("Rejoining server...").queue()
    LegitimooseBotClient.rejoin(force = true)
  }
}
