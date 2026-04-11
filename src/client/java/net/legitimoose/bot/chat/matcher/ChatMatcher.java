package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMatcher implements MessageMatcher {

    /* Matcher to match for chat messages, shouts or not.
       These are in the format <optional [SHOUT]> rank | player: message */

    public static final String COMMAND_PREFIX = "!";

    private static final Pattern PATTERN = Pattern.compile("^(?:\\[SHOUT]\\s*)?(?:[^|]+\\|\\s*)?([^:]+): (.*)", Pattern.DOTALL);

    private String username;

    private String message;

    private boolean isShout;

    private boolean isCommand;

    public ChatMatcher() {
        username = null;
        message = null;
        isShout = false;
        isCommand = false;
    }

    @Override
    public boolean matches(String message) {
        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        this.username = matcher.group(1);
        this.message = matcher.group(2);
        this.isShout = message.startsWith("[SHOUT]");
        this.isCommand = !isShout && message.startsWith(COMMAND_PREFIX);

        return true;
    }

    @Override
    public void visit(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleChatMessage(this, webhook);
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public boolean isShout() {
        return isShout;
    }

    public boolean isCommand() {
        return isCommand;
    }

}
