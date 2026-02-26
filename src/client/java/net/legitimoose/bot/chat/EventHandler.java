package net.legitimoose.bot.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.User;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.chat.command.BlockCommands;
import net.legitimoose.bot.chat.command.CommandSource;
import net.legitimoose.bot.chat.command.HelpCommand;
import net.legitimoose.bot.scraper.Ban;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Rank;
import net.legitimoose.bot.scraper.Scraper;
import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.discord.command.MsgCommand;
import net.legitimoose.bot.discord.command.ReplyCommand;
import net.legitimoose.bot.util.DiscordWebhook;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.bson.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class EventHandler {
    private static EventHandler INSTANCE;

    private final CommandDispatcher<CommandSource> dispatcher;

    public volatile List<String> lastMessages = new ArrayList<>();
    public boolean handleChat = true;

    private final Pattern chatPattern = Pattern.compile("^(?:\\[SHOUT]\\s*)?(?:[^|]+\\|\\s*)?([^:]+): (.*)", Pattern.DOTALL);
    private final Pattern msgPattern = Pattern.compile("\\[(.*) -> me] (?:(@\\S+) )?(.*)");

    private final Pattern joinPattern = Pattern.compile("^\\[\\+] (?:([^|]+) \\| )?(\\S+)");
    private final Pattern switchPattern = Pattern.compile("^\\[→]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern leavePattern = Pattern.compile("^\\[-]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    private final Pattern broadcastPattern = Pattern.compile("^\\[Broadcast\\]\\s(.*)");

    private final Pattern banPattern = Pattern.compile("^(\\S+)\\s+banned\\s+(\\S+)\\s+for\\s+'(.+)'");
    private final Pattern tempBanPattern = Pattern.compile("^(\\S+)\\s+tempbanned\\s+(\\S+)\\s+for\\s+(.+)\\s+hours\\s+for\\s+'(.+)'");
    private final Pattern unbanPattern = Pattern.compile("^(\\S+)\\s+unbanned\\s+(\\S+)\\s+for\\s+'(.+)'");

    public EventHandler(CommandDispatcher<CommandSource> dispatcher) {
        HelpCommand.register(dispatcher);
        BlockCommands.register(dispatcher);
        this.dispatcher = dispatcher;
    }

    public void onRecieveMessage(Component message, boolean overlay) throws CommandSyntaxException {
        lastMessages.add(message.getString());
        String msg = message.getString();
        String username = "";
        String cleanMessage = msg;

        Matcher joinMatcher = joinPattern.matcher(msg);
        Matcher switchMatcher = switchPattern.matcher(msg);
        Matcher leaveMatcher = leavePattern.matcher(msg);
        Matcher chatMatcher = chatPattern.matcher(msg);
        Matcher msgMatcher = msgPattern.matcher(msg);
        Matcher banMatcher = banPattern.matcher(msg);
        Matcher tempBanMatcher = tempBanPattern.matcher(msg);
        Matcher unbanMatcher = unbanPattern.matcher(msg);
        Matcher broadcastMatcher = broadcastPattern.matcher(msg);

        DiscordWebhook webhook = new DiscordWebhook(CONFIG.getString("webhook"));
        if (handleChat) {
            if (joinMatcher.find()) {
                MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);

                username = joinMatcher.group(2);
                String uuid;

                try {
                    uuid = McUtil.getUuid(username);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String rank = joinMatcher.group(1);
                if (rank == null) rank = "";
                if (players.countDocuments(new Document("uuid", uuid)) == 0) {
                    cleanMessage = String.format("**%s** joined the server for the first time!", username);
                } else {
                    cleanMessage = String.format("**%s** joined the server.", username);
                }

                new Player(uuid, username, Rank.getEnum(rank), List.of()).write();
                webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
                webhook.setContent(cleanMessage.replace("@", ""));
                try {
                    webhook.execute(0x57F287);
                } catch (IOException | URISyntaxException e) {
                    LOGGER.warn(e.getMessage());
                }
                return;
            } else if (switchMatcher.find()) {
                username = switchMatcher.group(1);
                cleanMessage = String.format("**%s** switched servers.", username);
                webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
                webhook.setContent(cleanMessage.replace("@", ""));
                try {
                    webhook.execute(0xF2F257);
                } catch (IOException | URISyntaxException e) {
                    LOGGER.warn(e.getMessage());
                }
                return;
            } else if (leaveMatcher.find()) {
                username = leaveMatcher.group(1);
                cleanMessage = String.format("**%s** left the server.", username);
                webhook.setEmbedThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
                webhook.setContent(cleanMessage.replace("@", ""));
                try {
                    webhook.execute(0xF25757);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else if (chatMatcher.find()) {
                username = chatMatcher.group(1);
                cleanMessage = chatMatcher.group(2);
                if (msg.startsWith("[SHOUT]")) {
                    webhook.setUsername(String.format("[SHOUT] %s", username));
                } else {
                    if (cleanMessage.startsWith("!")) {
                        dispatcher.execute(cleanMessage.substring(1), new CommandSource(username));
                    }
                    webhook.setUsername(username);
                }
                webhook.setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", username));
            } else if (msgMatcher.find()) {
                String username1 = msgMatcher.group(1);
                String username2 = msgMatcher.group(2);
                String msg1 = msgMatcher.group(3);
                User user;
                if (username2 != null) {
                    String finalUsername = username2.replace("@", "");
                    user =
                            DiscordBot.jda
                                    .getGuildById(CONFIG.getString("guildId"))
                                    .findMembers(s -> s.getUser().getName().equals(finalUsername))
                                    .get().getFirst().getUser();
                } else {
                    user = DiscordBot.jda.retrieveUserById(MsgCommand.lastSent.get(username1)).complete();
                }
                user.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(String.format("%s: %s", username1, msg1)))
                        .queue();
                ReplyCommand.lastSentReply.put(user.getIdLong(), username1);
                return;
            } else if (banMatcher.find()) {
                long ban_time = System.currentTimeMillis() / 1000L;
                String moderator = banMatcher.group(1);
                String banned = banMatcher.group(2);
                String reason = banMatcher.group(3);
                webhook.setContent(String.format("**%s** was banned by **%s**\nReason: %s", banned, moderator, reason));
                webhook.setUsername("Legitimoose Ban");
                try {
                    webhook.execute(0xF25757);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                try {
                    new Ban(ban_time, banned, McUtil.getUuid(banned), moderator, McUtil.getUuid(moderator), reason, -1, -1).write(); // For temp bans, calculate expiration time based on duration else -1 for permanent
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            } else if (tempBanMatcher.find()) {
                long ban_time = System.currentTimeMillis() / 1000L;
                String moderator = tempBanMatcher.group(1);
                String banned = tempBanMatcher.group(2);
                int hours = Integer.parseInt(tempBanMatcher.group(3));
                String reason = tempBanMatcher.group(4);
                webhook.setContent(String.format("**%s** was banned by **%s** for **%s** hours\nReason: %s", banned, moderator, hours, reason));
                webhook.setUsername("Legitimoose Ban");
                try {
                    webhook.execute(0xF25757);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                long duration = TimeUnit.HOURS.toSeconds(hours);
                try {
                    new Ban(ban_time, banned, McUtil.getUuid(banned), moderator, McUtil.getUuid(moderator), reason, duration, ban_time + duration).write(); // For temp bans, calculate expiration time based on duration else -1 for permanent
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            } else if (unbanMatcher.find()) {
                String moderator = unbanMatcher.group(1);
                String banned = unbanMatcher.group(2);
                String reason = unbanMatcher.group(3);
                webhook.setContent(String.format("**%s** was unbanned by **%s**\nReason: %s", banned, moderator, reason));
                webhook.setUsername("Legitimoose Ban");
                try {
                    webhook.execute(0x57F287);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else if (broadcastMatcher.find()) {
                String msg1 = broadcastMatcher.group(1);
                webhook.setUsername("[Broadcast]");
                webhook.setContent(msg1);
                try {
                    webhook.execute(0x5757F2);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            if (username.equals(Minecraft.getInstance().player.getName().getString()) && !FabricLoader.getInstance().isDevelopmentEnvironment())
                return;

            if (!username.isEmpty() &&
                    !cleanMessage.startsWith(CONFIG.getString("secretPrefix"))
            ) {
                webhook.setContent(cleanMessage.replace("@", ""));
                try {
                    webhook.execute();
                } catch (IOException | URISyntaxException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }

    public static EventHandler getInstance() {
        if (INSTANCE == null) INSTANCE = new EventHandler(new CommandDispatcher<>());
        return INSTANCE;
    }
}
