package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.LegitimooseBotClient;
import net.legitimoose.bot.discord.command.Command;

public class Rejoin implements Command {
    final SlashCommandInteractionEvent event;

    public Rejoin(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    @Override
    public void onCommandReceived() {
        event.deferReply(true).queue();
        event.getHook().sendMessage("Rejoining server...").queue();
        LegitimooseBotClient.rejoin(true);
    }
}
