package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for temporary bans. (including IP bans)
 * <p>
 * These are in the format <code>(moderator) temp( IP-)banned (player) for (hours) for (reason)</code>
 */
public class TempBanMatcher implements MessageMatcher {

    // private static final Pattern PATTERN = Pattern.compile("^(\\S+)\\s+tempbanned\\s+(\\S+)\\s+for\\s+(.+)\\s+hours\\s+for\\s+'(.+)'");
    private static final Pattern PATTERN = Pattern.compile("(\\w{3,16})\\s((?:temp)(?:\\s?IP-)?banned)\\s(\\w{3,16})\\sfor\\s(\\d*\\s(?:days?|hours?|minutes?|seconds?))(?:\\sfor\\s'(.*)')?");
    

    private String moderatorName;
    private String typeString;
    private String bannedName;
    private String banTimeString;
    private String reason;
    private int hours;

    public TempBanMatcher() {
        moderatorName = null;
        typeString = null;
        bannedName = null;
        banTimeString = null;
        reason = null;
        hours = 0;
    }

    @Override
    public boolean matches(String message) {
        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        moderatorName = matcher.group(1);
        typeString = matcher.group(2); 
        bannedName = matcher.group(3);
        banTimeString = matcher.group(4);
        if (banTimeString.contains("day")) {
            hours = Integer.parseInt(banTimeString.replace(" day", "").replace("s", "")) * 24;
        } else if (banTimeString.contains("hour")) {
            hours = Integer.parseInt(banTimeString.replace(" hour", "").replace("s", ""));
        } else if (banTimeString.contains("minute")) {
            hours = Integer.parseInt(banTimeString.replace(" minute", "").replace("s", "")) / 60;
        } else {
            hours = Integer.parseInt(banTimeString.replace(" second", "").replace("s", "")) / 3600;
        }
        reason = matcher.group(5); // is fine

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
    
    public String getBanType() {
    	return typeString;
    }
    
    public String getBanTimeString() {
    	return banTimeString;
    }

    public int getHours() {
        return hours;
    }

}
