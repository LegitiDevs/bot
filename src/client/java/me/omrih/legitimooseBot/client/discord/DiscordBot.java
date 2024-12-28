package me.omrih.legitimooseBot.client.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Collection;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

public class DiscordBot extends ListenerAdapter {
    static public void runBot() {
        JDA jda = JDABuilder.createDefault(CONFIG.discordToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        jda.addEventListener(new DiscordBot());
        jda.updateCommands().addCommands(
                Commands.slash("playerlist", "List the online players in the lobby"),
                Commands.slash("find", "Find which world a player is in").addOption(OptionType.STRING, "player", "The username of the player you want to find", true)
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("playerlist")) {
            Collection<PlayerListEntry> playerList = MinecraftClient.getInstance().getNetworkHandler().getPlayerList();
            StringBuilder players = new StringBuilder();
            for (PlayerListEntry player : playerList) {
                players.append(player.getDisplayName().getString()).append('\n');
            }
            event.reply(players.toString()).queue();
        } else if (event.getName().equals("find")) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("find " + event.getOption("player").getAsString());
            final Boolean[] bool = {true};
            ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
                if (!bool[0]) return;
                event.reply(message.getString().replace(" Click HERE to join.", "").trim()).queue();
                bool[0] = false;
            });
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        String discordNick = event.getMember().getEffectiveName().replace("§", "?");
        String message = "<br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>" + discordNick + "</yellow><gray>:</gray> " + event.getMessage().getContentStripped().replace("\n", "<br>").replace("§", "?");
        if (message.length() >= 200) return;
        if (event.getChannel().getId().equals(CONFIG.channelId())) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("lc " + message);
        }
    }
}
