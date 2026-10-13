package com.musiccurator.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.musiccurator.music.MusicController;
import com.musiccurator.music.SongSelector;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// resolve() draws the .ogg a sound instance will play from its event, once, right before playback.
// For background music that draw is handed to SongSelector, which honours the player's track selection.
// Hooking here rather than in WeighedSoundEvents#getSound gives one draw per song, on the top-level
// event: getSound also runs inside nested events (music.creative draws from music.game) and for every
// other sound in the game.
@Mixin(AbstractSoundInstance.class)
public abstract class AbstractSoundInstanceMixin {
	@Shadow
	@Final
	protected SoundSource source;

	// 26.3 renamed resolve() to getOrResolve(); the method body is unchanged.
	@WrapOperation(
			//? if >=26.3 {
			/*method = "getOrResolve",
			*///?} else {
			method = "resolve",
			//?}
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/sounds/WeighedSoundEvents;getSound(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/client/resources/sounds/Sound;"))
	private Sound musiccurator$selectSong(WeighedSoundEvents event, RandomSource random, Operation<Sound> original) {
		Sound vanillaPick = original.call(event, random);
		// In vanilla only MusicManager plays in the MUSIC category; jukebox discs use RECORDS.
		if (source != SoundSource.MUSIC || !MusicController.getInstance().isEnabled()) {
			return vanillaPick;
		}
		return SongSelector.choose(event, random, vanillaPick);
	}
}
