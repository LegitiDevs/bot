package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.LegitimooseBotClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ShoutCommand implements Command {
    final SlashCommandInteractionEvent event;
    final String message;
    private static final Map<Long, Long> cooldown = new HashMap<>();

    public ShoutCommand(SlashCommandInteractionEvent event, String message) {
        this.event = event;
        this.message = message;
    }

    @Override
    public void onCommandReceived() {
        long userId = event.getUser().getIdLong();
        Long lastUsed = cooldown.get(userId);
        if (lastUsed != null && System.currentTimeMillis() - lastUsed < TimeUnit.MINUTES.toMillis(1)) {
            event.reply(String.format("Can't shout now. Try again in %.0f seconds", Math.abs((System.currentTimeMillis() - lastUsed) * 0.001 - 60))).setEphemeral(true).queue();
            return;
        }
        String newMessage = ("[ᴅɪsᴄᴏʀᴅ] " + event.getMember().getEffectiveName() + ": " + message).replace("\n", "<br>").replace("§", "?");
        if (newMessage.length() >= 100) {
            event.reply("Failed to send, message too long!").setEphemeral(true).queue();
            return;
        }
        LegitimooseBotClient.mc.getConnection().sendCommand("shout " + newMessage);
        event.reply(String.format("Shouted `%s`", message.trim())).queue();
        cooldown.put(userId, System.currentTimeMillis());
    }
}
