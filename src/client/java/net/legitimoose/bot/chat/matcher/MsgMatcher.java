package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for private messages.
 * <p>
 * These are in the format <code>[(senderUsername) -> me] @(discordReceiver) (message)</code>
 */
public class MsgMatcher implements MessageMatcher {
    private static final Pattern PATTERN = Pattern.compile("\\[(.*) -> me] (?:(@\\S+) )?(.*)");

    private String senderUsername;

    private String discordReceiver;

    private String message;

    public MsgMatcher() {
        message = null;
        senderUsername = null;
        discordReceiver = null;
    }

    @Override
    public boolean matches(String message) {
        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        this.senderUsername = matcher.group(1);
        this.discordReceiver = matcher.group(2);
        this.message = matcher.group(3);

        return true;
    }

    @Override
    public void handle(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleMsgMessage(this);
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getDiscordReceiver() {
        return discordReceiver;
    }

    public String getMessage() {
        return message;
    }


}
