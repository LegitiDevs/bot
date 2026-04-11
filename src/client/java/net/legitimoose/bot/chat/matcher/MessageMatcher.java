package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

public interface MessageMatcher {

    /**
     * Gathers the required fields from the message
     *
     * @return whether the message is a match
     */
    boolean matches(String message);

    /**
     * Executes the corresponding handler for the message
     */
    void handle(GameChatHandler handler, DiscordWebhook webhook, Component original);
}