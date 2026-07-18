package net.legitimoose.bot.discord.command.staff;

import com.mongodb.client.FindIterable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.legitimoose.bot.discord.command.mute.BotMute;
import net.legitimoose.bot.scraper.Database;

public class MuteList extends ListenerAdapter {

    private static final int MUTES_PER_PAGE = 5;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("botmutelist"))
            return;

        event.deferReply(true).queue();

        int page = validatePage(event);
        if (page == -1)
            return;

        StringBuilder sb = new StringBuilder();
        int start = (page - 1) * MUTES_PER_PAGE;
        int added = 0;
        int maxLengthPerEntry = Message.MAX_CONTENT_LENGTH / MUTES_PER_PAGE;

        FindIterable<BotMute> mutes = Database.getBotMutes().find().skip(start);

        for (BotMute mute : mutes) {
            if (added < MUTES_PER_PAGE) {
                String string = (added + 1 + start) + ". " + mute.toString() + "\n";
                if (string.length() > maxLengthPerEntry) {
                    // maxLengthPerEntry - 4 to also remove the newline
                    sb.append(string, 0, maxLengthPerEntry - 4).append("...\n");
                } else {
                    sb.append(string);
                }
                added++;
            }
        }

        if (added == 0) {
            String message = start == 0 ? "No users are muted" : "Page " + page + " is empty";
            event.getHook().sendMessage(message).queue();
        } else {
            event.getHook().sendMessage(sb.toString()).queue();
        }
    }

    private int validatePage(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption("page");
        int page = option == null ? 1 : option.getAsInt();
        if (page < 1) {
            event.getHook().sendMessage("Invalid page number").setEphemeral(true).queue();
            return -1;
        }
        return page;
    }

}
