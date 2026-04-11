package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchMatcher implements MessageMatcher {

    /* Matcher to match for players switching worlds.
       These are in the format [→] Rank | Username */

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
    public void visit(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleSwitchMessage(this, webhook, original);
    }

    public String getUsername() {
        return username;
    }

}
