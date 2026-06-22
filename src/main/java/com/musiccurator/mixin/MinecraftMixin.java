package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.musiccurator.music.MusicController;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// getSituationalMusic() returns the track vanilla wants for the current context.
// We veto disabled tracks here (returning null = nothing plays) and, when asked,
// swap in our own delay range since MusicManager derives the gap from Music.minDelay/maxDelay.
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@ModifyReturnValue(method = "getSituationalMusic", at = @At("RETURN"))
	@Nullable
	private Music musiccurator$curate(@Nullable Music original) {
		MusicController controller = MusicController.getInstance();
		if (!controller.isEnabled()) {
			return original;
		}
		if (original == null) {
			return null;
		}
		if (controller.isPaused()) {
			return null;
		}

		Identifier id = original.sound().value().location();
		if (!controller.isTrackEnabled(id)) {
			return null;
		}

		if (controller.overrideDelay()) {
			return new Music(original.sound(), controller.minDelayTicks(), controller.maxDelayTicks(), original.replaceCurrentMusic());
		}
		return original;
	}
}
