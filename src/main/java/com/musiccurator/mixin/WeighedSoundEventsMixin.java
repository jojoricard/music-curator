package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.musiccurator.music.MusicController;
import com.musiccurator.music.SongSelector;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// The sound engine picks a random .ogg from each music event here. We replace that pick
// with one restricted to the player's selected songs (see SongSelector). Non-music sounds
// pass through untouched.
@Mixin(WeighedSoundEvents.class)
public class WeighedSoundEventsMixin {
	@ModifyReturnValue(
			method = "getSound(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/client/resources/sounds/Sound;",
			at = @At("RETURN"))
	private Sound musiccurator$filterMusic(Sound chosen, RandomSource random) {
		if (chosen == null) {
			return chosen;
		}
		Identifier loc = chosen.getLocation();
		if (loc == null || !loc.getPath().startsWith("music/")) {
			return chosen; // not background music
		}
		MusicController controller = MusicController.getInstance();
		if (!controller.isEnabled()) {
			return chosen;
		}
		return SongSelector.choose((WeighedSoundEvents) (Object) this, random, chosen);
	}
}
