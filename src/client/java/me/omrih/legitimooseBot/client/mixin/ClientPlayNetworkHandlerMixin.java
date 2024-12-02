package me.omrih.legitimooseBot.client.mixin;

import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Unique
    private static final Pattern JOIN_PATTERN = Pattern.compile("^\\[\\+]\\s*(?:[^|]+\\|\\s*)?(\\S+)");
    @Unique
    private static final Pattern CHAT_PATTERN = Pattern.compile("^(?:[^|]+\\|\\s*)?([^:]+):");

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    public void messageListener(GameMessageS2CPacket packet, CallbackInfo ci) {
        new Thread(() -> {
            try {
                String message = packet.content().getString();
                String username = "";
                String cleanMessage = message;
                boolean isJoinMessage = false;

                Matcher joinMatcher = JOIN_PATTERN.matcher(message);
                Matcher chatMatcher = CHAT_PATTERN.matcher(message);

                if (joinMatcher.find()) {
                    username = joinMatcher.group(1);
                    cleanMessage = "**Joined the server**";
                    isJoinMessage = true;
                } else if (chatMatcher.find()) {
                    username = chatMatcher.group(1);
                    cleanMessage = message.substring(chatMatcher.end()).trim();
                }

                if (!username.isEmpty()) {
                    DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
                    webhook.setUsername(username);
                    webhook.setAvatarUrl("https://mc-heads.net/avatar/" + username);

                    EmbedObject embed = new EmbedObject().setDescription(cleanMessage);
                    if (isJoinMessage) {
                        embed.setColor(Color.GREEN); // Green color for join messages
                    }

                    webhook.getEmbeds().add(embed);
                    webhook.execute();
                }
            } catch (Exception ignored) {
            }
        }).start();
    }
}