package com.musiccurator.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MusicManager.class)
public interface MusicManagerAccessor {
	// Force an immediate re-pick on skip/resume.
	@Accessor("nextSongDelay")
	void musiccurator$setNextSongDelay(int value);

	// Null once the current song has finished.
	@Accessor("currentMusic")
	@Nullable
	SoundInstance musiccurator$getCurrentMusic();
}
