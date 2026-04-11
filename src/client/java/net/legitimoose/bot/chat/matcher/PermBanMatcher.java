package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermBanMatcher implements MessageMatcher {

    /* Matcher to match for permanent bans.
       These are in the format <moderator> banned <banned> for <reason> */

    private static final Pattern PATTERN = Pattern.compile("^(\\S+)\\s+banned\\s+(\\S+)\\s+for\\s+'(.+)'");

    private String moderatorName;

    private String bannedName;

    private String reason;

    public PermBanMatcher() {
        moderatorName = null;
        bannedName = null;
        reason = null;
    }

    @Override
    public boolean matches(String message) {
        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        moderatorName = matcher.group(1);
        bannedName = matcher.group(2);
        reason = matcher.group(3);

        return true;
    }

    @Override
    public void visit(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handlePermBanMessage(this, webhook);
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

}
