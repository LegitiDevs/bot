package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.EventHandler;
import net.legitimoose.bot.LegitimooseBotClient;
import net.minecraft.client.Minecraft;

import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

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
            // Get /glist all and output
            EventHandler.getInstance().lastMessages.clear();
            Minecraft.getInstance().player.connection.sendCommand("glist all");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
            StringBuilder list = new StringBuilder();
            for (String message : EventHandler.getInstance().lastMessages) {
                list.append(message.trim() + "\n");
            }
            event.reply(String.format("```%s```", list)).queue();
        } else {
            // Get /listall and output
            EventHandler.getInstance().lastMessages.clear();
            Minecraft.getInstance().player.connection.sendCommand("listall");
            EventHandler.getInstance().joinLeaveMessages = false;
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
                EventHandler.getInstance().joinLeaveMessages = true;
            }
            EventHandler.getInstance().joinLeaveMessages = true;
            StringBuilder list = new StringBuilder();
            for (String message : EventHandler.getInstance().lastMessages) {
                list.append(message.trim() + "\n");
            }
            event.reply(String.format("```%s```", list)).queue();
        }
    }
}
