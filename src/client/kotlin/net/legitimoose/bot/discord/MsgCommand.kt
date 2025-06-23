import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.minecraft.client.Minecraft

class MsgCommand(
        override val event: SlashCommandInteractionEvent,
        val message: String,
        val player: String
) : Command {
    override fun onCommandReceived() {
        if ((message.length + player.length) >= 200) {
            event.reply("Failed to send, message and/or player name too long!")
                    .setEphemeral(true)
                    .queue()
            return
        }
        Minecraft.getInstance()
                .player
                ?.connection
                ?.sendCommand(
                        "msg " +
                                player.replace("§", "?") +
                                " [ᴅɪsᴄᴏʀᴅ] " +
                                event.member!!.effectiveName +
                                ": " +
                                message.replace("\n", "<br>").replace("§", "?")
                )
        event.reply("Sent `" + message.trim() + "` to " + player).setEphemeral(true).queue()
    }
}
