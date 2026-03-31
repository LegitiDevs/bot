package net.legitimoose.bot.chat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

import static com.mongodb.client.model.Filters.eq;

public class StreakCommand {
    private static final MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("streak")
                .then(LiteralArgumentBuilder.<CommandSource>literal("on")
                        .executes(context -> {
                            Player player = players.find(eq("name", context.getSource().username())).first();
                            assert player != null;
                            if (player.streak().notifications() == true) {
                                context.getSource().sendMessage("Your streak notifications are already enabled!");
                            } else {
                                players.updateOne(eq("name", context.getSource().username()), Updates.set("streak.notify", true));
                                context.getSource().sendMessage("Enabled streak notifications!");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(LiteralArgumentBuilder.<CommandSource>literal("off")
                        .executes(context -> {
                            Player player = players.find(eq("name", context.getSource().username())).first();
                            assert player != null;
                            if (player.streak().notifications() == false) {
                                context.getSource().sendMessage("Your streak notifications are already disabled!");
                            } else {
                                players.updateOne(eq("name", context.getSource().username()), Updates.set("streak.notify", true));
                                context.getSource().sendMessage("Disabled streak notifications!");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    Player player = players.find(eq("name", context.getSource().username())).first();
                    assert player != null;
                    context.getSource().sendMessage("Your current login streak is " + player.streak().days() + " days");
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
