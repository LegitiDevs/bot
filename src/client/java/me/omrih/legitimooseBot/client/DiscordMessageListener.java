package me.omrih.legitimooseBot.client;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Collection;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

public class DiscordMessageListener extends ListenerAdapter {
    static public void main() {
        JDA jda = JDABuilder.createDefault(CONFIG.discordToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        jda.addEventListener(new DiscordMessageListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        if (event.getMessage().getContentRaw().equals("!playerlist")) {
            Collection<PlayerListEntry> playerList = MinecraftClient.getInstance().getNetworkHandler().getPlayerList();
            StringBuilder players = new StringBuilder();
            for (PlayerListEntry player : playerList) {
                players.append(player.getDisplayName().getString()).append('\n');
            }
            event.getChannel().sendMessage(players).queue();
            return;
        }
        String discordNick = event.getMember().getEffectiveName().replace("§", "?");
        String message = "<br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>" + discordNick + "</yellow><gray>:</gray> " + event.getMessage().getContentStripped().replace("\n", "<br>").replace("§", "?");
        if (message.length() >= 200) return;
        if (event.getChannel().getId().equals(CONFIG.channelId())) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc " + message);
        }
    }
}
