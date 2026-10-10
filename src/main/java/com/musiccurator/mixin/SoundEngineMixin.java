package com.musiccurator.mixin;

import com.musiccurator.music.MusicController;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// resume() unpauses every channel, and vanilla calls it whenever a screen closes (Gui#setScreen), which
// would restart a song the player paused. Channel commands run in order on the sound thread, so pausing
// again right after wins.
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Inject(method = "resume", at = @At("TAIL"))
	private void musiccurator$keepMusicPaused(CallbackInfo ci) {
		MusicController.getInstance().reapplyPause();
	}
}
