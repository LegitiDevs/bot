package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.discord.CommandUtil;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ReplyCommand extends ListenerAdapter {
    public static final Map<Long, String> lastSentReply = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("reply")) return;
        String message = event.getOption("message").getAsString();
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
                .sendCommand(McUtil.sanitizeChat("msg " + newMessage));

        CommandUtil.replySanitized("Sent `" + message.trim() + "` to " + player, false, event);
    }
}
