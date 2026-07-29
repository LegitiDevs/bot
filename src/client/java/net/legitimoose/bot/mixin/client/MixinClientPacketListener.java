package net.legitimoose.bot.mixin.client;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.legitimoose.bot.LegitimooseBotClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.entity.player.Player;
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
