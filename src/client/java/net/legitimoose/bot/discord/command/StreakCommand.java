package net.legitimoose.bot.discord.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

public class StreakCommand extends ListenerAdapter {
    private final MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("streak")) return;
        switch (event.getSubcommandName()) {
            case "player" -> {
                String player = event.getOption("player").getAsString();
                event.deferReply().queue();

                Player dbPlayer = players.find(Filters.eq("name", player)).first();
                if (dbPlayer == null) {
                    event.getHook().sendMessage("Could not find a player named " + player).queue();
                    return;
                }
                event.getHook().sendMessage(player + "'s current login streak is " + dbPlayer.streak().days() + " days").queue();
            }
            case "lb", "leaderboard" -> {
                StreakLeaderboardHandler handler = new StreakLeaderboardHandler();
                event.getJDA().addEventListener(handler);
                handler.reply(event);
            }
        }
    }
}
