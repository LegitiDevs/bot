package net.legitimoose.bot.discord.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

import static com.mongodb.client.model.Sorts.descending;

public class StreakCommand extends ListenerAdapter {
    private final MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);
    int page;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        page = 1;
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
                event.reply(getLeaderboardString(page)).addComponents(ActionRow.of(Button.primary("back", Emoji.fromUnicode("⬅\uFE0F")), Button.primary("forward", Emoji.fromUnicode("➡\uFE0F")))).queue();

            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "back" -> {
                if (page == 1) {
                    event.deferEdit().queue();
                    return;
                } else {
                    page -= 1;
                }
                event.editMessage(getLeaderboardString(page)).queue();
            }
            case "forward" -> {
                page += 1;
                event.editMessage(getLeaderboardString(page)).queue();
            }
        }
    }

    private String getLeaderboardString(int page) {
        StringBuilder lbString = new StringBuilder("");
        int i = 1;
        for (Player player : players.find(Filters.exists("streak.days")).sort(descending("streak.days")).skip((page - 1) * 5).limit(5)) {
            lbString.append((page - 1) * 5 + i).append(". ").append(player.name()).append(" - ").append(player.streak().days()).append(" day(s)").append('\n');
            i++;
        }
        return lbString.toString().trim();
    }
}
