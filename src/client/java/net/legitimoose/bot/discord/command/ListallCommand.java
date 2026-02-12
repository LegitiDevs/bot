package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.http.endpoint.PlayersEndpoint;

public class ListallCommand implements Command {
    final SlashCommandInteractionEvent event;
    final boolean raw;

    public ListallCommand(SlashCommandInteractionEvent event, boolean raw) {
        this.event = event;
        this.raw = raw;
    }

    @Override
    public void onCommandReceived() {
        if (raw) {
            event.reply(String.format("```%s```", String.join("\n", new PlayersEndpoint().getGlist()))).queue();
        } else {
            event.reply(String.format("```%s```", String.join("\n", new PlayersEndpoint().getListall()))).queue();
        }
    }
}
