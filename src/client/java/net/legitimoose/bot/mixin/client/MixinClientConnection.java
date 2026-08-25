package net.legitimoose.bot.mixin.client;

import net.legitimoose.bot.LegitimooseBotClient;
import net.legitimoose.bot.chat.GameChatHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinClientConnection {
    // Wtf is this method name
    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundSystemChatPacket(Component content, boolean overlay)) {
            LegitimooseBotClient.getThreadPool().execute(() -> GameChatHandler.getInstance().handleChat(content));
        }
    }
}
