package com.musiccurator.mixin;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.client.sounds.WeighedSoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// Exposes the weighted pool of a sound event so we can pick directly among enabled songs.
@Mixin(WeighedSoundEvents.class)
public interface WeighedSoundEventsAccessor {
	@Accessor("list")
	List<Weighted<Sound>> musiccurator$getList();
}
