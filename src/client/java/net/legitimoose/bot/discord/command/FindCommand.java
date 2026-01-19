package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.LegitimooseBotClient;
import net.minecraft.client.Minecraft;

import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class FindCommand implements Command {
    final SlashCommandInteractionEvent event;
    final String player;

    public FindCommand(SlashCommandInteractionEvent event, String player) {
        this.event = event;
        this.player = player;
    }

    @Override
    public void onCommandReceived() {
        if (player.length() >= 200) {
            event.reply("player name too long, sorry!").setEphemeral(true).queue();
            return;
        }
        Minecraft.getInstance().player.connection.sendCommand("find " + player.replace("§", "?"));
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        event.reply(LegitimooseBotClient.lastMessages.getLast().replace(" Click HERE to join.", "").trim()).queue();
    }
}
