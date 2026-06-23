package com.musiccurator.music;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import com.musiccurator.mixin.MusicManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;

// Central runtime state. The mixins read from here; keybinds and commands write to it.
public final class MusicController {
	private static final MusicController INSTANCE = new MusicController();

	private boolean paused = false;
	// The actual .ogg currently playing (e.g. "music/game/sweden"), tracked for the HUD.
	@Nullable
	private Identifier currentSong = null;

	private MusicController() {
	}

	public static MusicController getInstance() {
		return INSTANCE;
	}

	private ModConfig cfg() {
		return ConfigManager.get();
	}

	public boolean isEnabled() {
		return cfg().enabled;
	}

	public boolean isPaused() {
		return paused;
	}

	// Per-song filter. Unknown songs (not in the registry / not disabled) always play.
	public boolean isSongEnabled(@Nullable Identifier soundLocation) {
		if (soundLocation == null) {
			return true;
		}
		return !cfg().disabledSongs.contains(soundLocation.getPath());
	}

	public int minDelayTicks() {
		return Math.max(1, cfg().minDelaySeconds) * 20;
	}

	public int maxDelayTicks() {
		ModConfig c = cfg();
		int min = Math.max(1, c.minDelaySeconds);
		// "Fixed interval" mode: no randomness, always wait exactly the min delay.
		if (c.overrideVanillaDelay) {
			return min * 20;
		}
		int max = Math.max(min, c.maxDelaySeconds);
		return max * 20;
	}

	// Rebuild the situational music with our configured frequency.
	public Music withDelays(Music original) {
		return new Music(original.sound(), minDelayTicks(), maxDelayTicks(), original.replaceCurrentMusic());
	}

	@Nullable
	public Identifier currentSong() {
		return currentSong;
	}

	public void skip() {
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		mm.stopPlaying();
		((MusicManagerAccessor) mm).musiccurator$setNextSongDelay(0);
	}

	public void togglePause() {
		paused = !paused;
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		if (paused) {
			mm.stopPlaying();
		} else {
			((MusicManagerAccessor) mm).musiccurator$setNextSongDelay(0);
		}
	}

	public boolean toggleHud() {
		ModConfig c = cfg();
		c.showHud = !c.showHud;
		ConfigManager.save();
		return c.showHud;
	}

	public void applyPreset(Preset preset) {
		if (preset == Preset.CUSTOM) {
			return;
		}
		ModConfig c = cfg();
		c.preset = preset;
		c.disabledSongs = Presets.disabledFor(preset);
		ConfigManager.save();
	}

	// Track the actual .ogg currently playing so the HUD can show its real title.
	public void clientTick() {
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		SoundInstance current = ((MusicManagerAccessor) mm).musiccurator$getCurrentMusic();
		if (current == null) {
			currentSong = null;
			return;
		}
		Sound sound = current.getSound();
		currentSong = sound != null ? sound.getLocation() : null;
	}
}
