package net.legitimoose.bot.chat.matcher;

import net.legitimoose.bot.chat.GameChatHandler;
import net.legitimoose.bot.util.DiscordWebhook;
import net.minecraft.network.chat.Component;

public interface MessageMatcher {

    /* This should return false if the message is not a match,
       however if it is a match, it should generate the sections
       which will be required later from the message before
       returning true */
    boolean matches(String message);

    /* Handles the execution of the Message
       For readability it should just call the relevant GameChatHandler method */
    void visit(GameChatHandler handler, DiscordWebhook webhook, Component original);

}