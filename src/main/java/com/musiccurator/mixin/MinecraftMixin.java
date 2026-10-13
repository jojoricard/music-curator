package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.musiccurator.music.MusicController;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// MusicManager asks getSituationalMusic() every tick which Music (sound event + delay range) fits the
// current context. Rewriting the delays applies the player's frequency. Track selection happens later,
// in AbstractSoundInstanceMixin; MusicManagerMixin handles the pause.
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@ModifyReturnValue(method = "getSituationalMusic", at = @At("RETURN"))
	@Nullable
	private Music musiccurator$curate(@Nullable Music original) {
		MusicController controller = MusicController.getInstance();
		if (!controller.isEnabled() || original == null) {
			return original;
		}
		return controller.withDelays(original);
	}
}
