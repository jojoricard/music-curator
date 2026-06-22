package com.musiccurator.music;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
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
		return cfg().enabledTracks.contains(id.toString());
	}

	public boolean overrideDelay() {
		return cfg().overrideVanillaDelay;
	}

	public int minDelayTicks() {
		return Math.max(1, cfg().minDelaySeconds) * 20;
	}

	public int maxDelayTicks() {
		int min = Math.max(1, cfg().minDelaySeconds);
		int max = Math.max(min, cfg().maxDelaySeconds);
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
