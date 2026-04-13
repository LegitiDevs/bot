package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.client.Minecraft;

public class Send extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("send")) return;
        String message = event.getOption("message").getAsString();
        event.deferReply(true).queue();
        if (message.isEmpty()) {
            event.getHook().sendMessage("Please provide a message to send.").queue();
            return;
        }
        Minecraft.getInstance().schedule(() -> {
            if (message.startsWith("/")) {
                Minecraft.getInstance().getConnection().sendCommand(message.substring(1));
                event.getHook().sendMessage(String.format("Command sent: `%s`", message)).queue();
                return;
            }
            Minecraft.getInstance().getConnection().sendChat(message);
            event.getHook().sendMessage(String.format("Message sent: `%s`", message)).queue();
        });
    }
}
