package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for players switching worlds.
 * <p>
 * These are in the format <code>[→] Rank | Username</code>
 */
public class SwitchMatcher implements MessageMatcher {
    private static final Pattern PATTERN = Pattern.compile("^\\[→]\\s*(?:[^|]+\\|\\s*)?(\\S+)");

    private String username;

    public SwitchMatcher() {
        username = null;
    }

    @Override
    public boolean matches(String message) {
        if (!message.startsWith("[→]"))
            return false;

        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        username = matcher.group(1);

        return true;
    }

    @Override
    public void handle(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleSwitchMessage(this, webhook, original);
    }

    public String getUsername() {
        return username;
    }

}
