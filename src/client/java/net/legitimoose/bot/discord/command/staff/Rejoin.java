package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.LegitimooseBotClient;

public class Rejoin extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rejoin")) return;
        event.deferReply(true).queue();
        event.getHook().sendMessage("Rejoining server...").queue();
        LegitimooseBotClient.attemptRejoin(true);
    }
}
