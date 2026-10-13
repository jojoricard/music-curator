package com.musiccurator.mixin;

import net.minecraft.client.resources.sounds.Sound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// A sounds.json entry of "type": "event" (music.creative lists all of music.game this way) is stored as
// an anonymous Weighted<Sound> created in SoundManager.Preparations#handleRegistration, not as a
// WeighedSoundEvents. This exposes the sounds.json entry it was built from, whose location is the event it
// points to, so SongSelector can list the tracks behind it.
// Both names below are compiler-generated (anonymous class, captured local "sound"): re-check them with
// javap when updating Minecraft. A renamed field fails at startup, but a renamed class only logs a Mixin
// warning and Creative mode silently loses the music.game tracks again.
// The class also captures the event id itself, but NeoForge's recompiled 1.21.x jars rename that field
// (val$resourcelocation, val$identifier instead of val$soundLocation); val$sound keeps its name everywhere.
@Mixin(targets = "net.minecraft.client.sounds.SoundManager$Preparations$1")
public interface SoundEventReferenceAccessor {
	@Accessor("val$sound")
	Sound musiccurator$getEntry();
}
