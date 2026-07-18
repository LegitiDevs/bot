package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.discord.command.mute.BotMuteHandler;

public class UnMute extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("unbotmute"))
            return;

        event.deferReply().queue();

        User user = event.getOption("discord_id").getAsUser();
        String minecraftName = event.getOption("minecraft_name").getAsString();

        boolean didDelete = BotMuteHandler.getInstance().delete(user.getId(), minecraftName);

        if (!didDelete)
            event.getHook().sendMessage("No bot mute exists for this person!").setEphemeral(true).queue();
        else
            event.getHook().sendMessage("<@" + user.getId() + "> | " + minecraftName + " has been unmuted from the bot").queue();
    }

}
