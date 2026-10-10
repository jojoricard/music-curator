package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.musiccurator.music.MusicController;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// MusicManager asks getSituationalMusic() every tick which Music (sound event + delay range) fits the
// current context. Rewriting the delays applies the player's frequency. Returning null while paused
// keeps MusicManager from starting a song (it does not stop the current one: togglePause() does).
// Track selection happens later, in AbstractSoundInstanceMixin.
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
