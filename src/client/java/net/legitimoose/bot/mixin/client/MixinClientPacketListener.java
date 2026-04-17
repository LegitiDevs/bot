package net.legitimoose.bot.mixin.client;

import net.legitimoose.bot.LegitimooseBotClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(at = @At("TAIL"), method = "handleLogin")
    public void legitiBot_handleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        LegitimooseBotClient.handleLogin();
    }
}
