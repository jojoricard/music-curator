package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.musiccurator.music.MusicController;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// getSituationalMusic() returns the track vanilla wants for the current context.
// We apply the configured frequency here (vanilla overworld gaps are 10-20 min) and
// honour the pause state. Per-song selection is handled in WeighedSoundEventsMixin.
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@ModifyReturnValue(method = "getSituationalMusic", at = @At("RETURN"))
	@Nullable
	private Music musiccurator$curate(@Nullable Music original) {
		MusicController controller = MusicController.getInstance();
		if (!controller.isEnabled() || original == null) {
			return original;
		}
		if (controller.isPaused()) {
			return null;
		}
		return controller.withDelays(original);
	}
}
