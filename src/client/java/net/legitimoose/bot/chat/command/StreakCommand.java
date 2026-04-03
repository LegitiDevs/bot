package net.legitimoose.bot.chat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

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
                                players.updateOne(eq("name", context.getSource().username()), Updates.set("streak.notify", false));
                                context.getSource().sendMessage("Disabled streak notifications!");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))

                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .executes(context -> {
                            String username = context.getArgument("username", String.class);
                            Player player = players.find(eq("name", username)).first();
                            if (player == null) {
                                context.getSource().sendMessage("Player not found!");
                                return Command.SINGLE_SUCCESS;
                            }
                            context.getSource().sendMessage(username + "'s current login streak is " + player.streak().days() + " days");
                            return Command.SINGLE_SUCCESS;
                        }))

                .then(LiteralArgumentBuilder.<CommandSource>literal("leaderboard").executes(context -> {
                    context.getSource().sendMessage(getLeaderboardString());
                    return Command.SINGLE_SUCCESS;
                }))

                .then(LiteralArgumentBuilder.<CommandSource>literal("lb").executes(context -> {
                    context.getSource().sendMessage(getLeaderboardString());
                    return Command.SINGLE_SUCCESS;
                }))

                .executes(context -> {
                    Player player = players.find(eq("name", context.getSource().username())).first();
                    assert player != null;
                    context.getSource().sendMessage("Your current login streak is " + player.streak().days() + " days");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static String getLeaderboardString() {
        StringBuilder lbString = new StringBuilder("<br>");
        int i = 1;
        for (Player player : players.find(Filters.exists("streak.days")).sort(descending("streak.days")).limit(5)) {
            lbString.append(i).append(". ").append(player.name()).append(" - ").append(player.streak().days()).append(" day(s)");
            if (i < 5) {
                lbString.append("<br>");
            }
            i++;
        }
        return lbString.toString();
    }
}
