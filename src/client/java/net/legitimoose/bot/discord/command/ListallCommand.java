package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.http.endpoint.PlayersEndpoint;
import net.legitimoose.bot.util.DiscordUtil;

public class ListallCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("listall")) return;

        boolean raw;
        if (event.getOption("raw") != null) {
            raw = event.getOption("raw").getAsBoolean();
        } else {
            raw = false;
        }

        if (raw) {
            event.reply(DiscordUtil.sanitizeString(String.format("```%s```", String.join("\n", new PlayersEndpoint().getGlist())))).queue();
        } else {
            event.reply(DiscordUtil.sanitizeString(String.format("```%s```", String.join("\n", new PlayersEndpoint().getListall())))).queue();
        }
    }
}
