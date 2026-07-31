package net.legitimoose.bot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.discord.command.mute.BotMuteHandler;
import net.legitimoose.bot.http.endpoint.PlayersEndpoint;
import net.legitimoose.bot.util.DiscordUtil;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListallCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("listall")) return;

        if (BotMuteHandler.getInstance().shouldCancelCommand(event)) {
            return;
        }

        List<Component> listall = new PlayersEndpoint().getListall();
        List<String> listallString = listall.stream().map(Component::getString).toList();
        event.reply(DiscordUtil.sanitizeString(String.format("```%s```", String.join("\n", listallString)))).queue();
    }
}
