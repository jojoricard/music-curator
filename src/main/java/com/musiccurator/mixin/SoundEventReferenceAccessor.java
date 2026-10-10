package com.musiccurator.mixin;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// A sounds.json entry of "type": "event" (music.creative lists all of music.game this way) is stored as
// an anonymous Weighted<Sound> created in SoundManager.Preparations#handleRegistration, not as a
// WeighedSoundEvents. This exposes the event it points to, so SongSelector can list the tracks behind it.
// Both names below are compiler-generated (anonymous class, captured local "soundLocation"): re-check
// them with javap when updating Minecraft. A renamed field fails at startup, but a renamed class only
// logs a Mixin warning and Creative mode silently loses the music.game tracks again.
@Mixin(targets = "net.minecraft.client.sounds.SoundManager$Preparations$1")
public interface SoundEventReferenceAccessor {
	@Accessor("val$soundLocation")
	Identifier musiccurator$getEvent();
}
