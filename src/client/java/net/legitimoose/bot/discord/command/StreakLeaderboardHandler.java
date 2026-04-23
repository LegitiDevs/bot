package net.legitimoose.bot.discord.command;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.scraper.Player;

import static com.mongodb.client.model.Sorts.descending;
import static net.legitimoose.bot.chat.command.StreakCommand.players;

public class StreakLeaderboardHandler extends ListenerAdapter {
    static int maxId = 0;
    int page = 1;
    int id;

    public StreakLeaderboardHandler() {
        id = maxId + 1;
        maxId++;
    }

    public void reply(SlashCommandInteractionEvent event) {
        event.reply(getLeaderboardString(page)).addComponents(ActionRow.of(Button.primary("back-" + id, Emoji.fromUnicode("⬅\uFE0F")), Button.primary("forward-" + id, Emoji.fromUnicode("➡\uFE0F")))).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId.equals("back-" + id)) {
            if (page == 1) {
                event.deferEdit().queue();
                return;
            } else {
                page -= 1;
            }
            event.editMessage(getLeaderboardString(page)).queue();
        } else if (componentId.equals("forward-" + id)) {
            page += 1;
            event.editMessage(getLeaderboardString(page)).queue();
        }
    }

    private String getLeaderboardString(int page) {
        StringBuilder lbString = new StringBuilder();
        int i = 1;
        for (Player player : players.find(Filters.exists("streak.days")).sort(descending("streak.days", "last_joined")).skip((page - 1) * 5).limit(5)) {
            lbString.append((page - 1) * 5 + i).append(". ").append(player.name()).append(" - ").append(player.streak().days()).append(" day(s)").append('\n');
            i++;
        }
        return lbString.toString().trim();
    }
}
