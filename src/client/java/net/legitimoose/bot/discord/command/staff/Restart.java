package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Restart extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("restart")) return;
        event.deferReply(true).queue();
        event.getHook().sendMessage("Restarting bot...").complete();
        System.exit(0);
    }
}
