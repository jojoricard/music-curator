package com.musiccurator.mixin;

import com.musiccurator.music.MusicController;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Record what is actually starting so the HUD can show it. Filtering already happened
// in MinecraftMixin, so anything reaching here is enabled.
@Mixin(MusicManager.class)
public class MusicManagerMixin {
	@Inject(method = "startPlaying(Lnet/minecraft/sounds/Music;)V", at = @At("HEAD"))
	private void musiccurator$onStart(Music music, CallbackInfo ci) {
		if (music == null) {
			return;
		}
		Identifier id = music.sound().value().location();
		MusicController.getInstance().onTrackStarted(id);
	}
}
