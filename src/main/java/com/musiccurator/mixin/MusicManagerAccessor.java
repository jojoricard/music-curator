package com.musiccurator.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Private MusicManager state used by MusicController.
@Mixin(MusicManager.class)
public interface MusicManagerAccessor {
	// Ticks left before the next song starts; 0 starts one on MusicManager's next tick (skip, resume).
	@Accessor("nextSongDelay")
	void musiccurator$setNextSongDelay(int value);

	// The song's sound instance; null between songs.
	@Accessor("currentMusic")
	@Nullable
	SoundInstance musiccurator$getCurrentMusic();
}
