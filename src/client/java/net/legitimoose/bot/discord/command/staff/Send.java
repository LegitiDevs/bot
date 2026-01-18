package net.legitimoose.bot.discord.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.legitimoose.bot.LegitimooseBotClient;
import net.legitimoose.bot.discord.command.Command;

public class Send implements Command {
    final SlashCommandInteractionEvent event;
    final String message;

    public Send(SlashCommandInteractionEvent event, String message) {
        this.event = event;
        this.message = message;
    }

    @Override
    public void onCommandReceived() {
        event.deferReply(true).queue();
        if (message.isEmpty()) {
            event.getHook().sendMessage("Please provide a message to send.").queue();
            return;
        }
        LegitimooseBotClient.mc.schedule(() -> {
            if (message.startsWith("/")) {
                LegitimooseBotClient.mc.getConnection().sendCommand(message.substring(1));
                event.getHook().sendMessage(String.format("Command sent: `%s`", message)).queue();
                return;
            }
            LegitimooseBotClient.mc.getConnection().sendChat(message);
            event.getHook().sendMessage(String.format("Message sent: `%s`", message)).queue();
        });
    }
}
