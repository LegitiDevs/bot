package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinMatcher implements MessageMatcher {

    /* Matcher to match for player join messages.
       These are in the format [+] Rank | Username or [+] Username */

    private static final Pattern PATTERN = Pattern.compile("^\\[\\+] (?:([^|]+) \\| )?(\\S+)");

    private String username;

    private String rank;

    public JoinMatcher() {
        username = null;
        rank = null;
    }

    @Override
    public boolean matches(String message) {
        // I believe quickly discarding incorrect messages this way first
        // is more efficient than calling Pattern#matcher on the message
        if (!message.startsWith("[+]"))
            return false;

        Matcher matcher = PATTERN.matcher(message);

        if (!matcher.find())
            return false;

        rank = matcher.group(1);
        if (rank == null) /* Preventing rank from being null avoids a null check in GameChatHandler */
            rank = "";
        username = matcher.group(2);

        return true;
    }

    @Override
    public void visit(GameChatHandler handler, DiscordWebhook webhook, Component original) {
        handler.handleJoinMessage(this, webhook);
    }

    public String getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

}
