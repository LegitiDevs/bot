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
        // Bot check I think (don't blame me I skimmed over everything) if (event.getMember() == null) return; 
        String message = event.getMessage().getContentStripped().replace("\n","<br>");
        String discordNick = event.getMember().getEffectiveName().replace("§","?");
        if (event.isWebhookMessage() || message.length() >= 200 || message.contains("§")) return;
        if (event.getChannel().getId().equals(CONFIG.channelId())) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc <br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>" + discordNick + "</yellow><gray>:</gray> " + message);
        }
    }
}
