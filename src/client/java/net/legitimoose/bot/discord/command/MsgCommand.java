package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.minecraft.client.Minecraft;

public class MsgCommand implements Command {
    final SlashCommandInteractionEvent event;
    final String message;
    final String player;

    public MsgCommand(SlashCommandInteractionEvent event, String message, String player) {
        this.event = event;
        this.message = message;
        this.player = player;
    }

    @Override
    public void onCommandReceived() {
        if ((message.length() + player.length()) >= 200) {
            event.reply("Failed to send, message and/or player name too long!").setEphemeral(true).queue();
            return;
        }

        Minecraft.getInstance()
                .player
                .connection
                .sendCommand(
                        "msg " +
                                player.replace("§", "?") +
                                " [ᴅɪsᴄᴏʀᴅ] " +
                                event.getMember().getEffectiveName() +
                                ": " +
                                message.replace("\n", "<br>").replace("§", "?"));
        event.reply("Sent `" + message.trim() + "` to " + player).setEphemeral(true).queue();
    }
}
