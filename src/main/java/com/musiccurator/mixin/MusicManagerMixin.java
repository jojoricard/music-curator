package com.musiccurator.mixin;

import com.musiccurator.music.MusicController;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// While paused, MusicManager must neither start a song nor replace or stop the paused one: skipping its
// whole tick does that. Having getSituationalMusic() return no music instead does not work everywhere:
// 1.21.1's tick() dereferences it, and NeoForge's tick() stops the current song when there is none.
// A music volume change made while paused fades in once playback resumes.
@Mixin(MusicManager.class)
public class MusicManagerMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void musiccurator$holdWhilePaused(CallbackInfo ci) {
		MusicController controller = MusicController.getInstance();
		if (controller.isEnabled() && controller.isPaused()) {
			ci.cancel();
		}
	}
}
