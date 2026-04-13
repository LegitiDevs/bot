package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.discord.CommandUtil;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;

public class ShoutCommand implements Command {

    private static final int MAX_SHOUT_LENGTH = 100;

    private static final Cooldown COOLDOWN = Cooldown.ofSeconds(30);

    final SlashCommandInteractionEvent event;

    final String content;

    public ShoutCommand(SlashCommandInteractionEvent event, String content) {
        this.event = event;
        // Trims the content to simulate the trim that occurs when a player sends
        // A chat message by default in MC.
        this.content = content.trim();
    }

    @Override
    public void onCommandReceived() {
        long userId = event.getUser().getIdLong();

        String username = CommandUtil.getInstigatorsName(event);
        boolean bypassCooldown = CommandUtil.isInstigatorManagerOfTargetGuild(event);

        if (!bypassCooldown) {
            long cooldown = COOLDOWN.getRemainingAndReset(userId);
            if (cooldown > 0) {
                CommandUtil.reply("Can't shout now. Try again in " + COOLDOWN.formatSeconds(cooldown) + " seconds", false, event);
                return;
            }
        }

        String message = "[ᴅɪsᴄᴏʀᴅ] " + username + ": " + content;

        if (message.length() >= MAX_SHOUT_LENGTH) {
            CommandUtil.reply("Failed to send, message too long!", false, event);
            return;
        }

        Minecraft.getInstance().getConnection().sendCommand("shout " + McUtil.sanitiseString(message));

        CommandUtil.replySanitised("Shouted '" + content + "'", true, event);
    }
}
