package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordUtil;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;

import java.util.concurrent.TimeUnit;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class FindCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("find")) return;
        String player = event.getOption("player").getAsString();
        if (!McUtil.isValidMCUsername(player)) {
            event.reply("player name too long, sorry!").setEphemeral(true).queue();
            return;
        }
        Minecraft.getInstance().player.connection.sendCommand(McUtil.sanitizeString("find " + player));
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        event.reply(DiscordUtil.sanitizeString(GameChatHandler.getInstance().lastMessages.getLast().replace(" Click HERE to join.", "").trim())).queue();
    }
}
