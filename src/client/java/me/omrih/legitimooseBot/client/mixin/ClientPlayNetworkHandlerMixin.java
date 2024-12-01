package me.omrih.legitimooseBot.client.mixin;

import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    public void messageListener(GameMessageS2CPacket packet, CallbackInfo ci) {
        new Thread(() -> {
            try {
                DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
                EmbedObject embed = new EmbedObject().setDescription(packet.content().toString());
                webhook.getEmbeds().add(embed);
                webhook.execute();
            } catch (Exception ignored) {
            }
        }).start();
    }
}
