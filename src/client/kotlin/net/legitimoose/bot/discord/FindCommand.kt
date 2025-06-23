import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

class FindCommand(override val event: SlashCommandInteractionEvent, val player: String) : Command {
    override fun onCommandReceived() {
        if (player.length >= 200) {
            event.reply("player name too long, sorry!").setEphemeral(true).queue()
            return
        }
        Minecraft.getInstance().player?.connection?.sendCommand("find " + player.replace("§", "?"))
        val bool: BooleanArray = booleanArrayOf(true)
        ClientReceiveMessageEvents.GAME.register { message: Component, _: Boolean ->
            if (!bool[0]) return@register
            event.reply(message.string.replace(" Click HERE to join.", "").trim()).queue()
            bool[0] = false
        }
    }
}
