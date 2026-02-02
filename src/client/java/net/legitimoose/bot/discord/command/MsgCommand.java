package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.minecraft.client.Minecraft;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

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
        String newMessage = player.replace("§", "?") + " [ᴅɪsᴄᴏʀᴅ] @" + event.getUser().getName() + ": " + message.replace("\n", "<br>").replace("§", "?");
        LOGGER.info(newMessage);

        if (newMessage.length() >= 200) {
            event.reply("Failed to send, message and/or player name too long!").setEphemeral(true).queue();
            return;
        }

        Minecraft.getInstance()
                .player
                .connection
                .sendCommand("msg " + newMessage);

        event.reply("Sent `" + message.trim() + "` to " + player).setEphemeral(true).queue();
    }
}
