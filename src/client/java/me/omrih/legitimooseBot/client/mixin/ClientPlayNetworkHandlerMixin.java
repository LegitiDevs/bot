package me.omrih.legitimooseBot.client.mixin;

import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    public void messageListener(GameMessageS2CPacket packet, CallbackInfo ci) {
        new Thread(() -> {
            try {
                String message = packet.content().getString();
                Pattern pattern = Pattern.compile("^(?:[^|]+\\|\\s*)?([^:]+):");
                Matcher matcher = pattern.matcher(message);

                String username = "";
                String cleanMessage = message;
                if (matcher.find()) {
                    username = matcher.group(1);
                    cleanMessage = message.substring(matcher.end()).trim();
                }
                DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
                webhook.setUsername(username);
                webhook.setAvatarUrl("https://mc-heads.net/avatar/" + username);
                EmbedObject embed = new EmbedObject().setDescription(cleanMessage);
                webhook.getEmbeds().add(embed);
                webhook.execute();
            } catch (Exception ignored) {
            }
        }).start();
    }
}
