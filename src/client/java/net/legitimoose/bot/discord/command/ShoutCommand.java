package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.LegitimooseBotClient;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;

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

        boolean bypassCooldown;
        String username;
        if (event.getMember() == null) {
            bypassCooldown = false;
            username = event.getUser().getEffectiveName();
        } else {
            bypassCooldown = event.getMember().getPermissions().contains(Permission.MANAGE_SERVER) && event.getGuild().getId().equals(CONFIG.getOrDefault("discordGuildId", "1311574348989071440"));
            username = event.getMember().getEffectiveName();
        }

        if (lastUsed != null && System.currentTimeMillis() - lastUsed < TimeUnit.SECONDS.toMillis(30) && !bypassCooldown) {
            event.reply(String.format("Can't shout now. Try again in %.0f seconds", Math.abs((System.currentTimeMillis() - lastUsed) * 0.001 - 30))).setEphemeral(true).queue();
            return;
        }

        String newMessage = ("[ᴅɪsᴄᴏʀᴅ] " + username + ": " + message).replace("\n", "<br>").replace("§", "?");
        if (newMessage.length() >= 100) {
            event.reply("Failed to send, message too long!").setEphemeral(true).queue();
            return;
        }
        Minecraft.getInstance().getConnection().sendCommand("shout " + newMessage);
        event.reply(String.format("Shouted `%s`", message.trim())).queue();
        cooldown.put(userId, System.currentTimeMillis());
    }
}
