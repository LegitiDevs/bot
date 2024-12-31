package me.omrih.legitimooseBot.client.discord;

import com.mojang.brigadier.suggestion.Suggestion;
import me.omrih.legitimooseBot.client.mixin.ChatInputSuggestorAccessor;
import me.omrih.legitimooseBot.client.mixin.ChatScreenAccessor;
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
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

public class DiscordBot extends ListenerAdapter {
    static public void runBot() {
        JDA jda = JDABuilder.createDefault(CONFIG.discordToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        jda.addEventListener(new DiscordBot());
        jda.updateCommands().addCommands(
                Commands.slash("list", "List online players in the server").addOption(OptionType.BOOLEAN, "lobby", "True if you only want to see online players in the lobby"),
                Commands.slash("find", "Find which world a player is in").addOption(OptionType.STRING, "player", "The username of the player you want to find", true),
                Commands.slash("msg", "Message an ingame player").addOption(OptionType.STRING, "player", "The username of the player you want to message", true).addOption(OptionType.STRING, "message", "The message you want to send", true)).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("list")) {
            if (event.getOption("lobby") != null && event.getOption("lobby").getAsBoolean()) {
                Collection<PlayerListEntry> playerList = MinecraftClient.getInstance().getNetworkHandler().getPlayerList();
                StringBuilder players = new StringBuilder();
                for (PlayerListEntry player : playerList) {
                    players.append(player.getDisplayName().getString()).append('\n');
                }
                event.reply(players.toString()).queue();
            } else {
                event.deferReply().queue();
                MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new ChatScreen("/find ")));

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen screen) {
                    ChatInputSuggestor chatInputSuggestor = ((ChatScreenAccessor) screen).getChatInputSuggestor();
                    chatInputSuggestor.show(false);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    StringBuilder suggestions = new StringBuilder();
                    try {
                        for (Suggestion suggestion : ((ChatInputSuggestorAccessor) chatInputSuggestor).getPendingSuggestions().get().getList()) {
                            suggestions.append(suggestion.getText() + '\n');
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                    event.getHook().sendMessage(suggestions.toString()).queue();

                } else {
                    event.getHook().sendMessage("it didn't work :shrug:").queue();
                }
            }
        } else if (event.getName().equals("find")) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("find " + event.getOption("player").getAsString());
            final Boolean[] bool = {true};
            ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
                if (!bool[0]) return;
                event.reply(message.getString().replace(" Click HERE to join.", "").trim()).queue();
                bool[0] = false;
            });
        } else if (event.getName().equals("msg")) {
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand("msg " + event.getOption("player").getAsString() + " [ᴅɪsᴄᴏʀᴅ] " + event.getMember().getEffectiveName() + ": " + event.getOption("message").getAsString().replace("\n", "<br>").replace("§", "?"));
            event.reply("Sent " + event.getOption("message").getAsString().trim() + " to " + event.getOption("player").getAsString()).setEphemeral(true).queue();
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
