import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface Command {
  val event: SlashCommandInteractionEvent

  fun onCommandReceived()
}
