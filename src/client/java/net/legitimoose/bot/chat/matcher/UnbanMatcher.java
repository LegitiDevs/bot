package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnbanMatcher implements MessageMatcher {

    /* Matcher to match for unbans.
       These are in the format <moderator> unbanned <unbanned> for <reason> */

    private static final Pattern PATTERN = Pattern.compile("^(\\S+)\\s+unbanned\\s+(\\S+)\\s+for\\s+'(.+)'");

    private String moderatorName;

    private String unbannedName;

    private String reason;

    public UnbanMatcher() {
        moderatorName = null;
        unbannedName = null;
        reason = null;
    }

    @Override
    public boolean matches(String message) {
        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        moderatorName = matcher.group(1);
        unbannedName = matcher.group(2);
        reason = matcher.group(3);

        return true;
    }

    @Override
    public void visit(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleUnbanMessage(this, webhook);
    }

    public String getModerator() {
        return moderatorName;
    }

    public String getUnbanned() {
        return unbannedName;
    }

    public String getReason() {
        return reason;
    }

}
