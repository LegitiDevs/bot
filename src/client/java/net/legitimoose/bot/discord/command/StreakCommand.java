package net.legitimoose.bot.discord.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

public class StreakCommand implements Command {
    private final MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);
    private final SlashCommandInteractionEvent event;
    private final String username;

    public StreakCommand(SlashCommandInteractionEvent event, String username) {
        this.event = event;
        this.username = username;
    }

    @Override
    public void onCommandReceived() {
        event.deferReply().queue();

        Player player = players.find(Filters.eq("name", username)).first();
        if (player == null) {
            event.getHook().sendMessage("Could not find a player named " + username).queue();
            return;
        }
        event.getHook().sendMessage(username + "'s current login streak is " + player.streak().days() + " days").queue();
    }
}
