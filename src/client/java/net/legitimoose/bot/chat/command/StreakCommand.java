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
                            if (player.streak() != 0) {
                                context.getSource().sendMessage("Your login streak is already enabled!");
                            } else {
                                players.updateOne(eq("name", context.getSource().username()), Updates.set("streak", 1));
                                context.getSource().sendMessage("Enabled login streak!");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(LiteralArgumentBuilder.<CommandSource>literal("off")
                        .executes(context -> {
                            Player player = players.find(eq("name", context.getSource().username())).first();
                            assert player != null;
                            if (player.streak() == 0) {
                                context.getSource().sendMessage("Your login streak is already disabled!");
                            } else {
                                players.updateOne(eq("name", context.getSource().username()), Updates.set("streak", 0));
                                context.getSource().sendMessage("Disabled login streak!");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    Player player = players.find(eq("name", context.getSource().username())).first();
                    assert player != null;
                    if (player.streak() != 0) {
                        context.getSource().sendMessage("Your current login streak is " + player.streak());
                    } else {
                        context.getSource().sendMessage("Enable your login streak with !streak on!");
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
