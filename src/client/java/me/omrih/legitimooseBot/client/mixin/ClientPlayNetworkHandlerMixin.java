package me.omrih.legitimooseBot.client.mixin;

import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.CONFIG;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onChatMessage", at = @At("HEAD"))
    public void messageListener(ChatMessageS2CPacket packet, CallbackInfo ci) {
        try {
            DiscordWebhook webhook = new DiscordWebhook(CONFIG.webhookUrl());
            webhook.setUsername(packet.sender().toString());
            webhook.setAvatarUrl("https://mc-heads.net/avatar/" + packet.sender());
            EmbedObject embed = new EmbedObject().setDescription(packet.body().content());
            webhook.getEmbeds().add(embed);
            webhook.execute();
        } catch (Exception ignored) {
        }
    }
}
