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
        String message = event.getMessage().getContentStripped().replace("\n", "<br>").replace("§", "?");
        if (event.isWebhookMessage() || message.length() >= 200) return;
        String discordNick = event.getMember().getEffectiveName().replace("§", "?");
        if (event.getChannel().getId().equals(CONFIG.channelId())) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc <br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>" + discordNick + "</yellow><gray>:</gray> " + message);
        }
    }
}
