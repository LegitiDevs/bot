package net.legitimoose.bot.mixin.client;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public class MixinFramrateLimitTracker {
    @Inject(method = "getFramerateLimit", cancellable = true, at= @At("HEAD"))
    public void disableFramerateLimit(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Minecraft.getInstance().options.framerateLimit().get());
    }
}
