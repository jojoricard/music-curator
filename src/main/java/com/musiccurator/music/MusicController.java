package com.musiccurator.music;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.mixin.MusicManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

// Central runtime state. The mixins read from here; keybinds and commands write to it.
public final class MusicController {
	private static final MusicController INSTANCE = new MusicController();

	private boolean paused = false;
	@Nullable
	private Identifier currentTrack = null;

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

	public boolean isTrackEnabled(@Nullable Identifier id) {
		if (id == null) {
			return false;
		}
		String key = id.toString();
		// Tracks the mod doesn't know about are never muted: we only filter what the
		// user has explicitly disabled in the registry. This keeps unexpected/new
		// vanilla music playing instead of going silent.
		if (TrackRegistry.get(key) == null) {
			return true;
		}
		return cfg().enabledTracks.contains(key);
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

	public void onTrackStarted(Identifier id) {
		this.currentTrack = id;
	}

	public void onTrackStopped() {
		this.currentTrack = null;
	}

	@Nullable
	public Identifier currentTrack() {
		return currentTrack;
	}

	public void skip() {
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		mm.stopPlaying();
		((MusicManagerAccessor) mm).musiccurator$setNextSongDelay(0);
		onTrackStopped();
	}

	public void togglePause() {
		paused = !paused;
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		if (paused) {
			mm.stopPlaying();
			onTrackStopped();
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
		c.enabledTracks = Presets.tracksFor(preset);
		ConfigManager.save();
	}

	// Clear the HUD once the current song actually finishes.
	public void clientTick() {
		if (currentTrack == null) {
			return;
		}
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		if (((MusicManagerAccessor) mm).musiccurator$getCurrentMusic() == null) {
			onTrackStopped();
		}
	}
}
