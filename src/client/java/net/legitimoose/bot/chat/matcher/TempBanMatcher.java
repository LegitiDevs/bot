package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for temporary bans.
 * <p>
 * These are in the format <code>(moderator) tempbanned (player) for (hours) for (reason)</code>
 */
public class TempBanMatcher implements MessageMatcher {

    private static final Pattern PATTERN = Pattern.compile("^(\\S+)\\s+tempbanned\\s+(\\S+)\\s+for\\s+(.+)\\s+hours\\s+for\\s+'(.+)'");

    private String moderatorName;

    private String bannedName;

    private String reason;

    private int hours;

    public TempBanMatcher() {
        moderatorName = null;
        bannedName = null;
        reason = null;
        hours = 0;
    }

    @Override
    public boolean matches(String message) {
        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        moderatorName = matcher.group(1);
        bannedName = matcher.group(2);
        hours = Integer.parseInt(matcher.group(3));
        reason = matcher.group(4);

        return true;
    }

    @Override
    public void handle(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleTempBanMessage(this, webhook);
    }

    public String getModerator() {
        return moderatorName;
    }

    public String getBanned() {
        return bannedName;
    }

    public String getReason() {
        return reason;
    }

    public int getHours() {
        return hours;
    }

}
