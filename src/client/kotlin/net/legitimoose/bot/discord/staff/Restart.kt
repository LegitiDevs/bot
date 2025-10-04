import kotlin.system.exitProcess
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class Restart(override val event: SlashCommandInteractionEvent) : Command {
  override fun onCommandReceived() {
    event.hook.sendMessage("Restarting bot...").complete()
    exitProcess(0)
  }
}
