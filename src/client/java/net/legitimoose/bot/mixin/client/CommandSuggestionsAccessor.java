package net.legitimoose.bot.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {
    @Accessor
    CompletableFuture<Suggestions> getPendingSuggestions();
}
