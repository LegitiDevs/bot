package net.legitimoose.bot.discord;

import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.legitimoose.bot.discord.command.*;
import net.legitimoose.bot.discord.command.staff.Rejoin;
import net.legitimoose.bot.discord.command.staff.Restart;
import net.legitimoose.bot.discord.command.staff.Send;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;

import java.util.List;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class DiscordBot extends ListenerAdapter {
    public static JDA jda;

    public static void run() {
        List<ListenerAdapter> commands = List.of(
                new FindCommand(),
                new ListallCommand(),
                new ListCommand(),
                new MsgCommand(),
                new ReplyCommand(),
                new ShoutCommand(),
                new StreakCommand(),

                new Restart(),
                new Rejoin(),
                new Send()
        );
        jda = JDABuilder.createDefault(CONFIG.getString("token"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build();

        jda.addEventListener(new DiscordBot());
        for (EventListener command : commands) {
            jda.addEventListener(command);
        }
        jda.updateCommands()
                .addCommands(
                        Commands.slash("list", "List online players in the server"),
                        Commands.slash("find", "Find which world a player is in")
                                .addOption(
                                        OptionType.STRING,
                                        "player",
                                        "The username of the player you want to find",
                                        true),
                        Commands.slash("msg", "Message an ingame player")
                                .addOption(
                                        OptionType.STRING,
                                        "player",
                                        "The username of the player you want to message",
                                        true)
                                .addOption(OptionType.STRING, "message", "The message you want to send", true),
                        Commands.slash("listall", "List all online worlds with the players in them")
                                .addOption(
                                        OptionType.BOOLEAN,
                                        "raw",
                                        "True if you want to output world UUIDs instead of the world name"),
                        Commands.slash("shout", "Send a shout message")
                                .addOption(
                                        OptionType.STRING,
                                        "message",
                                        "The message to shout",
                                        true
                                ),
                        Commands.slash("reply", "Reply to an incoming message")
                                .addOption(
                                        OptionType.STRING,
                                        "message",
                                        "The reply to send",
                                        true
                                ),
                        Commands.slash("streak", "Streak-related commands")
                                .addSubcommands(
                                        new SubcommandData("player", "Get a player's streak")
                                                .addOption(OptionType.STRING, "player", "The player whose streak you want to check", true),
                                        new SubcommandData("lb", "Leaderboard")
                                ))
                .queue();
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (!event.getGuild().getId().equals(CONFIG.getString("guildId"))) return;
        event.getGuild()
                .updateCommands()
                .addCommands(
                        Commands.slash("rejoin", "Rejoin server")
                                .setDefaultPermissions(
                                        DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
                        Commands.slash("restart", "Restart bot")
                                .setDefaultPermissions(
                                        DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)),
                        Commands.slash("send", "Send message")
                                .setDefaultPermissions(
                                        DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                                .addOption(OptionType.STRING, "message", "The message to send", true))
                .queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String discordNick;
        if (event.isWebhookMessage()) {
            if (!event.getAuthor().getId().equals(CONFIG.getString("bridgeWebhookId"))) return;
            discordNick = event.getAuthor().getEffectiveName();
        } else {
            discordNick = event.getMember().getEffectiveName();
        }
        Component formattedMesssage = MinecraftSerializer.INSTANCE.serialize(event.getMessage().getContentDisplay());
        String message =
                String.format("<br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>%s</yellow><dark_gray>:</dark_gray> ", discordNick) +
                        MiniMessage.miniMessage().serialize(formattedMesssage);
        if (!event.getMessage().getAttachments().isEmpty()) {
            message += " <blue>[Attachment Included]</blue>";
        }
        if (CONFIG.getString("channelId").isEmpty())
            LOGGER.error("Discord channel ID is not set in config!");
        if (event.getChannel().getId().equals(CONFIG.getString("channelId"))) {
            Minecraft.getInstance().player.connection.sendChat(McUtil.sanitizeString(message));
        }
    }
}
