package me.omrih.legitimooseBot.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Command;
import me.omrih.legitimooseBot.client.Scraper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ScrapeCommand {
    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("scrape").executes(context -> {
            Scraper.scrapeAll();
            return Command.SINGLE_SUCCESS;
        }));
    }
}
