package net.legitimoose.bot.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.User;
import net.fabricmc.loader.api.FabricLoader;
import net.legitimoose.bot.chat.command.BlockCommands;
import net.legitimoose.bot.chat.command.CommandSource;
import net.legitimoose.bot.chat.command.HelpCommand;
import net.legitimoose.bot.chat.command.StreakCommand;
import net.legitimoose.bot.chat.matcher.*;
import net.legitimoose.bot.discord.DiscordBot;
import net.legitimoose.bot.discord.command.MsgCommand;
import net.legitimoose.bot.discord.command.ReplyCommand;
import net.legitimoose.bot.scraper.Ban;
import net.legitimoose.bot.scraper.Player;
import net.legitimoose.bot.scraper.Rank;
import net.legitimoose.bot.scraper.Scraper;
import net.legitimoose.bot.util.DiscordUtil;
import net.legitimoose.bot.util.DiscordWebhook;
import net.legitimoose.bot.util.DiscordWebhook.Embed;
import net.legitimoose.bot.util.McUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static net.legitimoose.bot.LegitimooseBot.CONFIG;
import static net.legitimoose.bot.LegitimooseBot.LOGGER;

public class GameChatHandler {

    private static GameChatHandler instance;

    private final CommandDispatcher<CommandSource> dispatcher;

    public volatile List<String> lastMessages = new ArrayList<>();
    public boolean handleChat = true;

    private final Pattern worldPattern = Pattern.compile("(?<=joined\\s)(.*)(?=\\s+Click to Join)");

    private final List<MessageMatcher> matchers;

    public GameChatHandler() {
        this.dispatcher = new CommandDispatcher<>();

        HelpCommand.register(dispatcher);
        BlockCommands.register(dispatcher);
        StreakCommand.register(dispatcher);

        // Ordered for efficiency B)
        matchers = List.of(
                new JoinMatcher(),
                new SwitchMatcher(),
                new LeaveMatcher(),
                new ChatMatcher(),
                new MsgMatcher(),
                new TempBanMatcher(),
                new PermBanMatcher(),
                new UnbanMatcher(),
                new BroadcastMatcher()
        );
    }

    public void handleChat(Component component) {
        String message = component.getString();
        lastMessages.add(message);

        if (handleChat) {
            DiscordWebhook webhook = new DiscordWebhook(CONFIG.getString("webhook"));
            handleChat(component, message, webhook);
        }
    }

    private void handleChat(Component original, String message, DiscordWebhook webhook) {
        for (MessageMatcher matcher : matchers) {
            if (matcher.matches(message)) {
                matcher.handle(this, webhook, original);
                return;
            }
        }
        // Not necessary but might be useful
        LOGGER.warn("Could not interpret message '{}'", message);
    }

    public void handleBroadcastMessage(BroadcastMatcher broadcast, DiscordWebhook webhook) {
        String message = broadcast.getMessage();
        webhook.setUsername("[Broadcast]");
        Embed embed = new Embed(DiscordUtil.sanitizeString(message), 0x5757F2);
        executeWebhook(webhook, embed, true);
    }

    public void handleUnbanMessage(UnbanMatcher unban, DiscordWebhook webhook) {
        String moderator = unban.getModerator();
        String unbanned = unban.getUnbanned();
        String reason = unban.getReason();
        Embed embed = new Embed(DiscordUtil.sanitizeString(String.format("**%s** was unbanned by **%s**", unbanned, moderator)), 0x57F287);
        embed.setDescription(DiscordUtil.sanitizeString(reason));
        webhook.setUsername("Legitimoose Ban");
        executeWebhook(webhook, embed, true);
    }

    public void handleTempBanMessage(TempBanMatcher tempBan, DiscordWebhook webhook) {
        long time = System.currentTimeMillis() / 1000L;
        String moderator = tempBan.getModerator();
        String banned = tempBan.getBanned();
        int hours = tempBan.getHours();
        String reason = tempBan.getReason();
        Embed embed = new Embed(DiscordUtil.sanitizeString(String.format("**%s** was unbanned by **%s** for **%s** hours", banned, moderator, hours)), 0xF25757);
        embed.setDescription(DiscordUtil.sanitizeString(reason));
        webhook.setUsername("Legitimoose Ban");
        executeWebhook(webhook, embed, true);
        long duration = TimeUnit.HOURS.toSeconds(hours);
        Ban.writeTempBan(time, banned, moderator, reason, duration);
    }

    public void handlePermBanMessage(PermBanMatcher permBan, DiscordWebhook webhook) {
        long time = System.currentTimeMillis() / 1000L;
        String moderator = permBan.getModerator();
        String banned = permBan.getBanned();
        String reason = permBan.getReason();
        Embed embed = new Embed(DiscordUtil.sanitizeString(String.format("**%s** was unbanned by **%s**", banned, moderator)), 0xF25757);
        embed.setDescription(DiscordUtil.sanitizeString(reason));
        webhook.setUsername("Legitimoose Ban");
        executeWebhook(webhook, embed, true);
        Ban.writePermBan(time, banned, moderator, reason);
    }

    public void handleMsgMessage(MsgMatcher msg) {
        String senderUsername = msg.getSenderUsername();
        String discordReceiverName = msg.getDiscordReceiver();
        String message = msg.getMessage();
        User user;
        if (discordReceiverName != null) {
            String finalUsername = discordReceiverName.replace("@", "");
            user = DiscordBot.jda.getGuildById(CONFIG.getString("guildId"))
                    .findMembers(s -> s.getUser().getName().equals(finalUsername))
                    .get().getFirst().getUser();
        } else {
            user = DiscordBot.jda.retrieveUserById(MsgCommand.lastSent.get(senderUsername)).complete();
        }
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(String.format("%s: %s", senderUsername, message)))
                .queue();
        ReplyCommand.lastSentReply.put(user.getIdLong(), senderUsername);
    }

    /**
     * Handles a bot command sent by a user. See {@link #handleChatMessage}.
     */
    private void handleCommandMessage(String command, String senderUsername) {
        try {
            dispatcher.execute(command, new CommandSource(senderUsername));
        } catch (CommandSyntaxException e) {
            LOGGER.error("Error handling command", e);
        }
    }

    public void handleChatMessage(ChatMatcher chat, DiscordWebhook webhook) {
        String username = chat.getUsername();
        String message = chat.getMessage();

        if (!shouldLogMessage(username, message)) {
            return;
        }

        if (chat.isShout()) {
            webhook.setUsername(String.format("[SHOUT] %s", username));
        } else {
            if (chat.isCommand())
                handleCommandMessage(message.substring(1), username);
            // Currently bot command messages are sent to Discord
            webhook.setUsername(username);
        }
        webhook.setAvatarUrl(String.format("https://mc-heads.net/avatar/%s", username));
        webhook.setContent(DiscordUtil.sanitizeString(message));
        executeWebhook(webhook, null, false);
    }

    public void handleLeaveMessage(LeaveMatcher leave, DiscordWebhook webhook) {
        String username = leave.getUsername();
        String messageToSend = String.format("**%s** left the server.", username);
        Embed embed = new Embed(DiscordUtil.sanitizeString(messageToSend), 0xF25757);
        embed.setThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
        executeWebhook(webhook, embed, true);
    }

    public void handleSwitchMessage(SwitchMatcher transfer, DiscordWebhook webhook, Component component) {
        String username = transfer.getUsername();
        String messageToSend = String.format("**%s** switched servers.", username);
        Component hover = ((HoverEvent.ShowText) component.getStyle().getHoverEvent()).value();
        String world = hover.getString();
        Matcher worldMatcher = worldPattern.matcher(world);
        worldMatcher.find();
        String cleanWorld = worldMatcher.group(1);
        Embed embed = new Embed(DiscordUtil.sanitizeString(messageToSend), 0xF2F257);
        embed.setDescription(DiscordUtil.sanitizeString("Joined " + cleanWorld));
        embed.setThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
        executeWebhook(webhook, embed, false);
    }

    public void handleJoinMessage(JoinMatcher join, DiscordWebhook webhook) {
        Instant time = Instant.now();
        MongoCollection<Player> players = Scraper.getInstance().db.getCollection("players", Player.class);
        String username = join.getUsername();
        Player dbPlayer = players.find(eq("name", username)).first();

        String uuid = McUtil.getUuidOrThrow(username);
        int days;
        boolean notify;
        Instant lastJoined;
        String rank = join.getRank();

        String messageToSend;
        if (dbPlayer == null) {
            lastJoined = time;
            days = 1;
            notify = false;
            messageToSend = String.format("**%s** joined the server for the first time!", username);
        } else {
            if (dbPlayer.last_joined() != null) {
                lastJoined = dbPlayer.last_joined();
            } else {
                lastJoined = time;
            }
            if (dbPlayer.streak() == null) {
                days = 1;
                notify = false;
            } else {
                days = dbPlayer.streak().days();
                if (dbPlayer.streak().notifications() == null) {
                    notify = false;
                } else {
                    notify = dbPlayer.streak().notifications();
                }
            }
            messageToSend = String.format("**%s** joined the server.", username);
        }

        long difference = ChronoUnit.DAYS.between(lastJoined.truncatedTo(ChronoUnit.DAYS), time.truncatedTo(ChronoUnit.DAYS));
        if (difference == 1) {
            days++;
        } else if (difference > 1) {
            if (notify)
                Minecraft.getInstance().player.connection.sendChat(String.format("%s's streak of %s days has been reset!", username, days));
            days = 1;
        }

        new Player(uuid, username, Rank.getEnum(rank), List.of(), new Player.Streak(days, notify), time).write();
        Embed embed = new Embed(DiscordUtil.sanitizeString(messageToSend), 0x57F287);
        embed.setThumbnail(String.format("https://mc-heads.net/head/%s/50/left", username));
        executeWebhook(webhook, embed, false);
    }

    private void executeWebhook(DiscordWebhook webhook, Embed embed, boolean throwErrors) {
        try {
            webhook.execute(embed);
        } catch (IOException | URISyntaxException e) {
            if (throwErrors)
                throw new RuntimeException(e);
            LOGGER.warn(e.getMessage());
        }
    }

    private boolean shouldLogMessage(String senderName, String message) {
        if (senderName.isEmpty() || (!FabricLoader.getInstance().isDevelopmentEnvironment() &&
                Minecraft.getInstance().player.getPlainTextName().equals(senderName)))
            return false;

        return !message.startsWith(CONFIG.getString("secretPrefix"));
    }

    public static GameChatHandler getInstance() {
        return instance == null ? (instance = new GameChatHandler()) : instance;
    }
}
