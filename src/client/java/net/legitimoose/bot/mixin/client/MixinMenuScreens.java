package net.legitimoose.bot.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuScreens.ScreenConstructor.class)
public interface MixinMenuScreens {
    @Inject(method = "fromPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"), cancellable = true)
    default <T extends AbstractContainerMenu> void cancelGuiRendering(Component title, MenuType<T> type, Minecraft minecraft, int containerId, CallbackInfo ci) {
        ci.cancel();
    }
}
