package com.musiccurator.mixin;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.client.sounds.WeighedSoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// Exposes a sound event's private weighted entries so SongSelector can list its tracks. Each entry is
// a Sound (one .ogg) or a reference to another event (see SoundEventReferenceAccessor).
@Mixin(WeighedSoundEvents.class)
public interface WeighedSoundEventsAccessor {
	@Accessor("list")
	List<Weighted<Sound>> musiccurator$getList();
}
