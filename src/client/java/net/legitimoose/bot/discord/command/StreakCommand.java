package net.legitimoose.bot.discord.command;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.scraper.Database;
import net.legitimoose.bot.scraper.Player;

public class StreakCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("streak")) return;
        switch (event.getSubcommandName()) {
            case "player" -> {
                String player = event.getOption("player").getAsString();
                event.deferReply().queue();

                Player dbPlayer = Database.getPlayers().find(Filters.eq("name", player)).first();
                if (dbPlayer == null) {
                    event.getHook().sendMessage("Could not find a player named " + player).queue();
                    return;
                }
                event.getHook().sendMessage(player + "'s current login streak is " + dbPlayer.streak().days() + " days").queue();
            }
            case "lb", "leaderboard" -> {
                // super jank. could cause a memory leak probably
                StreakLeaderboardHandler handler = new StreakLeaderboardHandler();
                event.getJDA().addEventListener(handler);
                handler.reply(event);
            }
        }
    }
}
