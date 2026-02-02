package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ReplyCommand implements Command {
    final SlashCommandInteractionEvent event;
    final String message;
    public static final Map<Long, String> lastSentReply = new HashMap<>();

    public ReplyCommand(SlashCommandInteractionEvent event, String message) {
        this.event = event;
        this.message = message;
    }

    @Override
    public void onCommandReceived() {
        if (lastSentReply.get(event.getUser().getIdLong()) == null) {
            event.reply("You have no incoming messages to reply.").setEphemeral(true).queue();
            return;
        }

        String player = lastSentReply.get(event.getUser().getIdLong());
        String newMessage = player.replace("§", "?") + " [ᴅɪsᴄᴏʀᴅ] @" + event.getUser().getName() + ": " + message.replace("\n", "<br>").replace("§", "?");

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
