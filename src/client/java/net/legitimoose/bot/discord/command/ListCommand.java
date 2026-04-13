package net.legitimoose.bot.discord.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.legitimoose.bot.scraper.Scraper;
import net.legitimoose.bot.util.DiscordUtil;
import net.minecraft.client.Minecraft;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class ListCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("list")) return;
        MongoCollection<Document> coll = Scraper.getInstance().db.getCollection("stats");

        event
                .deferReply()
                .queue(); // It *does* send a packet to the mc server, so keeping this is safer...

        // Please ignore the nulls. Only the 'input' is actually used
        CommandContext context = new CommandContextBuilder(null, null, null, 1).build("/find ");

        CompletableFuture<Suggestions> pendingParse =
                Minecraft.getInstance()
                        .player
                        .connection
                        .getSuggestionsProvider()
                        .customSuggestion(context);

        pendingParse.thenRun(() -> {
            if (!pendingParse.isDone()) {
                LOGGER.warn("Pending parse is not done! (somehow??)");
                return;
            }
            List<Suggestion> mcSuggestions = pendingParse.join().getList();
            StringBuilder suggestions = new StringBuilder();
            for (Suggestion suggestion : mcSuggestions) {
                suggestions.append(suggestion.getText() + '\n');
            }
            event.getHook()
                    .sendMessage(
                            DiscordUtil.sanitizeString(String.format("There are %s player(s) online:\n```\n%s```", mcSuggestions.size(), suggestions)))
                    .queue();

            coll.insertOne(new Document().append("_id", new ObjectId()).append("player_count", mcSuggestions.size()));
        });
    }
}

