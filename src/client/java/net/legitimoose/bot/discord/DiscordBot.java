package net.legitimoose.bot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.legitimoose.bot.LegitimooseBot;
import net.legitimoose.bot.discord.command.FindCommand;
import net.legitimoose.bot.discord.command.ListCommand;
import net.legitimoose.bot.discord.command.MsgCommand;
import net.legitimoose.bot.discord.command.staff.Rejoin;
import net.legitimoose.bot.discord.command.staff.Restart;
import net.legitimoose.bot.discord.command.staff.Send;
import net.minecraft.client.Minecraft;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class DiscordBot extends ListenerAdapter {
    public static JDA jda;

    public static void run() {
        jda =
                JDABuilder.createDefault(CONFIG.getOrDefault("discordToken", ""))
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                        .build();

        jda.addEventListener(new DiscordBot());
        jda.updateCommands()
                .addCommands(
                        Commands.slash("list", "List online players in the server")
                                .addOption(
                                        OptionType.BOOLEAN,
                                        "lobby",
                                        "True if you only want to see online players in the lobby"),
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
                                .addOption(OptionType.STRING, "message", "The message you want to send", true))
                .queue();
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (!event.getGuild().getId().equals(CONFIG.getOrDefault("discordGuildId", "1311574348989071440"))) return;
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "list" -> {
                boolean lobby;
                if (event.getOption("lobby") != null) {
                    lobby = event.getOption("lobby").getAsBoolean();
                } else {
                    lobby = false;
                }
                new ListCommand(event, lobby).onCommandReceived();
            }
            case "find" -> new FindCommand(event, event.getOption("player").getAsString()).onCommandReceived();
            case "msg" ->
                    new MsgCommand(event, event.getOption("message").getAsString(), event.getOption("player").getAsString()).onCommandReceived();
            case "rejoin" -> new Rejoin(event).onCommandReceived();
            case "restart" -> new Restart(event).onCommandReceived();
            case "send" -> new Send(event, event.getOption("message").getAsString()).onCommandReceived();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        String discordNick = event.getMember().getEffectiveName().replace("§", "?");
        String message =
                String.format("<br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>%s</yellow><dark_gray>:</dark_gray> ", discordNick) +
                        event.getMessage().getContentStripped().replace("\n", "<br>").replace("§", "?");
        if (!event.getMessage().getAttachments().isEmpty()) {
            message += " <blue>[Attachment Included]</blue>";
        }
        if (message.length() >= 200) return;
        if (CONFIG.getOrDefault("channelId", "").isEmpty())
            LOGGER.error("Discord channel ID is not set in config!");
        if (event.getChannel().getId().equals(CONFIG.getOrDefault("channelId", ""))) {
            Minecraft.getInstance().player.connection.sendChat(message);
        }
    }
}
