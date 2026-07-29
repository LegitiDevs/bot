package net.legitimoose.bot.chat.command;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.legitimoose.bot.chat.command.argument.BlockPosArgumentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class BotCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("bot")
                        .then(
                                LiteralArgumentBuilder.<CommandSource>literal("goto")
                                        .then(
                                                RequiredArgumentBuilder.<CommandSource, BlockPos>argument(
                                                        "where",
                                                        new BlockPosArgumentType()
                                                ).executes(context -> {
                                                    CommandSource source = context.getSource();
                                                    BlockPos where = context.getArgument("where", BlockPos.class);

                                                    BaritoneAPI.getProvider()
                                                            .getPrimaryBaritone()
                                                            .getCustomGoalProcess()
                                                            .setGoalAndPath(new GoalBlock(where));

                                                    source.sendMessage("Going to " + where.toShortString());
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSource>literal("follow")
                                        .then(
                                                RequiredArgumentBuilder.<CommandSource, String>argument(
                                                        "player",
                                                        StringArgumentType.string()
                                                ).executes(context -> {
                                                    CommandSource source = context.getSource();
                                                    String player = context.getArgument("player", String.class);

                                                    BaritoneAPI.getProvider()
                                                            .getPrimaryBaritone()
                                                            .getFollowProcess()
                                                            .follow((entity) -> {
                                                                if (entity instanceof Player) {
                                                                    return ((Player) entity).getName().getString().equals(player);
                                                                }
                                                                return false;
                                                            });

                                                    source.sendMessage("Following " + player);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSource>literal("cancel").executes((context -> {
                                    BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                                    context.getSource().sendMessage("Cancelled bot actions.");
                                    return Command.SINGLE_SUCCESS;
                                }))
                        )
        );
    }
}
