package com.musiccurator.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

// The audio channel of each sound being played, so the music's channel alone can be paused and resumed.
@Mixin(SoundEngine.class)
public interface SoundEngineAccessor {
	@Accessor("instanceToChannel")
	Map<SoundInstance, ChannelAccess.ChannelHandle> musiccurator$getInstanceToChannel();
}
