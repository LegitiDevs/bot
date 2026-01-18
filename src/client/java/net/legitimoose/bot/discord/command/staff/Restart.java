package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.discord.command.Command;

public class Restart implements Command {
    final SlashCommandInteractionEvent event;

    public Restart(SlashCommandInteractionEvent event) {
        this.event = event;
    }


    @Override
    public void onCommandReceived() {
        event.deferReply(true).queue();
        event.getHook().sendMessage("Restarting bot...").complete();
        System.exit(0);
    }
}
