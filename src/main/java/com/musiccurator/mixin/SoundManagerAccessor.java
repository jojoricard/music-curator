package com.musiccurator.mixin;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// SoundManager offers no per-sound pause; MusicController reaches the engine's channels through this.
@Mixin(SoundManager.class)
public interface SoundManagerAccessor {
	@Accessor("soundEngine")
	SoundEngine musiccurator$getSoundEngine();
}
