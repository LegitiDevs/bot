package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.legitimoose.bot.discord.command.mute.BotMute;
import net.legitimoose.bot.discord.command.mute.BotMuteHandler;
import net.legitimoose.bot.util.TimeUtil;

public class Mute extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("botmute"))
            return;

        event.deferReply().queue();

        String timeString = event.getOption("duration").getAsString();

        long endTime = validateDuration(timeString, event.getHook());
        if (endTime == -1)
            return;

        User user = event.getOption("discord_id").getAsUser();
        String minecraftName = event.getOption("minecraft_name").getAsString();
        String reason = event.getOption("reason").getAsString();

        if (!validateReason(reason, event.getHook()))
            return;

        if (!validateUser(user.getId(), minecraftName, event.getHook()))
            return;

        BotMuteHandler.getInstance().add(new BotMute(minecraftName, user.getId(), endTime, reason));

        String notice = "User <@" + user.getId() + "> | " + minecraftName +
                " has been muted from the bot for " + timeString + " due to " + reason;

        event.getHook().sendMessage(notice).queue();
    }

    private boolean validateReason(String reason, InteractionHook hook) {
        if (reason.length() > 50) {
            hook.sendMessage("Reason is too long, max 50 characters").queue();
            return false;
        }
        return true;
    }

    private boolean validateUser(String discordId, String minecraftName, InteractionHook hook) {
        if (BotMuteHandler.getInstance().contains(discordId, minecraftName)) {
            hook.sendMessage("User is already muted").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private long validateDuration(String duration, InteractionHook hook) {
        long time = TimeUtil.parse(duration);
        if (time == -1) {
            hook.sendMessage("Invalid duration given").setEphemeral(true).queue();
            return -1;
        }
        return time + System.currentTimeMillis();
    }

}
