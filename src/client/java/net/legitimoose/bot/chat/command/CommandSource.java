package net.legitimoose.bot.chat.command;

import net.minecraft.client.Minecraft;

public record CommandSource(String username) {
    public void sendMessage(String message) {
        if (message.length() > 256) {
            message = message.substring(0, 253) + "..";
        }
        Minecraft.getInstance().getConnection().sendChat(message);
    }
}
