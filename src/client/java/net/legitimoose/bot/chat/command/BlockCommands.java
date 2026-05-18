package net.legitimoose.bot.chat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.legitimoose.bot.scraper.Database;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class BlockCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        // Block
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("block")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String blocked = context.getArgument("username", String.class);
                            Database.getPlayers().updateOne(eq("name", source.username()), Updates.set("blocked", List.of(blocked)));
                            source.sendMessage("Blocked @" + blocked + " from sending you messages");
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            List<String> blockedPlayers = Database.getPlayers().find(eq("name", source.username())).first().blocked();
                            source.sendMessage("Blocked players:<br>" + String.join("<br>", blockedPlayers));
                            return Command.SINGLE_SUCCESS;
                        })));
        // Unblock
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("unblock")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String blocked = context.getArgument("username", String.class);
                            Database.getPlayers().updateOne(eq("name", source.username()), Updates.pull("blocked", blocked));
                            source.sendMessage("Unblocked @" + blocked + " from sending you messages");
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
