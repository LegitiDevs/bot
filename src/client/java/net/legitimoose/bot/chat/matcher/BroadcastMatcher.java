package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for broadcasts.
 * <p>
 * These are in the format <code>[Broadcast] (message)</code>
 */
public class BroadcastMatcher implements MessageMatcher {
    private static final Pattern PATTERN = Pattern.compile("^\\[Broadcast\\]\\s(.*)");

    private String message;

    public BroadcastMatcher() {
        message = null;
    }

    @Override
    public boolean matches(String message) {
        // Only using first two characters as they differentiate it from all others anyway
        if (!message.startsWith("[B"))
            return false;

        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        this.message = matcher.group(1);

        return true;
    }

    @Override
    public void handle(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleBroadcastMessage(this, webhook);
    }

    public String getMessage() {
        return message;
    }

}
