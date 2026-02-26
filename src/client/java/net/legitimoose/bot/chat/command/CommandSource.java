package net.legitimoose.bot.chat.command;

import net.minecraft.client.Minecraft;

public record CommandSource(String username) {
    public void sendMessage(String message) {
        if (message.length() >= 200) {
            return;
        }
        Minecraft.getInstance().getConnection().sendChat(message);
    }
}
