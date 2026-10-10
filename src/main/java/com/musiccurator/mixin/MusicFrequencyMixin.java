package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.musiccurator.music.MusicController;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// MusicManager draws the gap before the next song here, capped by vanilla's Music Frequency option
// (Default: 20 min, Frequent: 10 min, Constant: always 5 s). For the music MinecraftMixin gave the
// player's delays, those delays win, so the option no longer overrides the mod's settings.
@Mixin(MusicManager.MusicFrequency.class)
public class MusicFrequencyMixin {
	@ModifyReturnValue(method = "getNextSongDelay", at = @At("RETURN"))
	private int musiccurator$usePlayerDelays(int vanillaDelay, @Nullable Music music, RandomSource random) {
		if (music == null || !MusicController.getInstance().usesPlayerFrequency(music)) {
			return vanillaDelay;
		}
		return Mth.nextInt(random, music.minDelay(), music.maxDelay());
	}
}
