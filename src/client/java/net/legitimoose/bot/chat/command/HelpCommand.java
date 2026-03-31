package net.legitimoose.bot.chat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class HelpCommand {
    private static final String helpMessage = "Commands:<br>!block <user> - Block <user> from sending you messages<br>!unblock <user> - Unblock <user><br>!streak - View your join streak<br>!streak <on|off> - Enable or disable streak notifications";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("help")
                .executes(context -> {
                    context.getSource().sendMessage(helpMessage);
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
