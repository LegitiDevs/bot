package net.legitimoose.bot.chat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Scraper;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class BlockCommands {
    private static final MongoCollection<Player> coll = Scraper.getInstance().db.getCollection("players", Player.class);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        // Block
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("block")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String blocked = context.getArgument("username", String.class);
                            coll.updateOne(eq("name", source.username()), Updates.set("blocked", List.of(blocked)));
                            source.sendMessage("Blocked @" + blocked + " from sending you messages");
                            return Command.SINGLE_SUCCESS;
                        })));
        // Unblock
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("unblock")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String blocked = context.getArgument("username", String.class);
                            coll.updateOne(eq("name", source.username()), Updates.pull("blocked", blocked));
                            source.sendMessage("Unblocked @" + blocked + " from sending you messages");
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
