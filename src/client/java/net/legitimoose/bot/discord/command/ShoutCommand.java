package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.discord.CommandUtil;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;

public class ShoutCommand extends ListenerAdapter {

    private static final int MAX_SHOUT_LENGTH = 100;

    private static final Cooldown COOLDOWN = Cooldown.ofSeconds(30);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("shout")) return;
        String content = event.getOption("message").getAsString();

        long userId = event.getUser().getIdLong();
        String username = CommandUtil.getInstigatorsName(event);
        boolean bypassCooldown = CommandUtil.isInstigatorManagerOfTargetGuild(event);

        if (!bypassCooldown) {
            long cooldown = COOLDOWN.getRemainingAndSet(userId);
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

        Minecraft.getInstance().getConnection().sendCommand("shout " + McUtil.sanitizeString(message));

        CommandUtil.replySanitised("Shouted '" + content + "'", true, event);
    }
}
