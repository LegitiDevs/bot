package me.omrih.legitimooseBot.client;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.client.MinecraftClient;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

public class DiscordMessageListener extends ListenerAdapter {
    static public void main() {
        JDA jda = JDABuilder.createDefault(CONFIG.discordToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        jda.addEventListener(new DiscordMessageListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getMessage().getContentDisplay().length() >= 200 || event.getMessage().getContentDisplay().contains("§")) return;
        if (event.getChannel().getId().equals(CONFIG.channelId())) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc [Discord] " + "<" + event.getMember().getEffectiveName() + ">: " + event.getMessage().getContentDisplay().replace("\n","<br>"));
        }
    }
}
