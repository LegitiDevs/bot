import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.minecraft.client.Minecraft

class ShoutCommand(override val event: SlashCommandInteractionEvent, val message: String) :
        Command {
    override fun onCommandReceived() {
        if (message.length >= 200) {
            event.reply("Failed to send, message too long!").setEphemeral(true).queue()
            return
        }
        Minecraft.getInstance()
                .player
                ?.connection
                ?.sendCommand(
                        "shout [ᴅɪsᴄᴏʀᴅ] " +
                                event.member!!.effectiveName +
                                ": " +
                                message.replace("\n", "<br>").replace("§", "?")
                )
        event.reply("Successfully shouted `${message.trim()}`").setEphemeral(true).queue()
    }
}
