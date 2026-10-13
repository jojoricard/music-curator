package com.musiccurator.music;

import com.mojang.blaze3d.audio.Channel;
import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import com.musiccurator.mixin.MusicManagerAccessor;
import com.musiccurator.mixin.SoundEngineAccessor;
import com.musiccurator.mixin.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import org.jetbrains.annotations.Nullable;

// Runtime state and the actions behind keybinds and commands; the mixins and the HUD read from it.
// Settings are read from ConfigManager on every call, so edits apply immediately.
public final class MusicController {
	private static final MusicController INSTANCE = new MusicController();
	private static final int TICKS_PER_SECOND = 20;

	// Session-only (not in ModConfig): every launch starts with music playing.
	private boolean paused = false;
	// The song whose audio channel the pause froze; null when paused between two songs.
	@Nullable
	private SoundInstance pausedMusic = null;
	// The track playing or paused (e.g. "music/game/sweden"), for the HUD; null between songs.
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

	// Tracks missing from tracks.json (e.g. from resource packs) cannot be disabled, so they always play.
	public boolean isSongEnabled(Identifier soundLocation) {
		return !cfg().disabledSongs.contains(soundLocation.getPath());
	}

	public boolean isFavorite(Identifier soundLocation) {
		return cfg().favoriteSongs.contains(soundLocation.getPath());
	}

	public int minDelaySeconds() {
		return Math.max(1, cfg().minDelaySeconds);
	}

	public int maxDelaySeconds() {
		ModConfig c = cfg();
		// "Fixed interval" mode: no randomness, always wait exactly the min delay.
		if (c.overrideVanillaDelay) {
			return minDelaySeconds();
		}
		// The two sliders are independent: a max below the min collapses to the min.
		return Math.max(minDelaySeconds(), c.maxDelaySeconds);
	}

	// Background music in a world, which the player's delays apply to. The title screen (MENU) and the
	// scores vanilla chains without any gap (dragon fight, end credits: 0-0 delays) keep the game's timing.
	public boolean usesPlayerFrequency(Music music) {
		//? if >1.21.1 {
		return isEnabled() && music != Musics.MENU && music.maxDelay() != 0;
		//?} else {
		/*return isEnabled() && music != Musics.MENU && music.getMaxDelay() != 0;
		*///?}
	}

	public Music withDelays(Music original) {
		if (!usesPlayerFrequency(original)) {
			return original;
		}
		//? if >1.21.1 {
		return new Music(original.sound(), minDelaySeconds() * TICKS_PER_SECOND, maxDelaySeconds() * TICKS_PER_SECOND,
				original.replaceCurrentMusic());
		//?} else {
		/*return new Music(original.getEvent(), minDelaySeconds() * TICKS_PER_SECOND, maxDelaySeconds() * TICKS_PER_SECOND,
				original.replaceCurrentMusic());
		*///?}
	}

	@Nullable
	public Identifier currentSong() {
		return currentSong;
	}

	// Skipping while paused resumes playback with the next track.
	public void skip() {
		paused = false;
		pausedMusic = null;
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		// stopPlaying() schedules vanilla's usual gap; zeroing it starts the next song right away.
		mm.stopPlaying();
		((MusicManagerAccessor) mm).musiccurator$setNextSongDelay(0);
	}

	public boolean togglePause() {
		setPaused(!paused);
		return paused;
	}

	// Pausing freezes the song's audio channel, so resuming continues it where it stopped. MusicManagerMixin
	// keeps MusicManager from starting another song meanwhile.
	private void setPaused(boolean pause) {
		if (pause == paused) {
			return;
		}
		paused = pause;
		Minecraft minecraft = Minecraft.getInstance();
		MusicManager mm = minecraft.getMusicManager();
		SoundInstance current = ((MusicManagerAccessor) mm).musiccurator$getCurrentMusic();
		if (pause) {
			pausedMusic = current != null && minecraft.getSoundManager().isActive(current) ? current : null;
			if (pausedMusic != null) {
				setChannelPaused(pausedMusic, true);
			}
			return;
		}
		if (pausedMusic != null && pausedMusic == current && minecraft.getSoundManager().isActive(current)) {
			setChannelPaused(current, false);
		} else {
			// Paused between two songs, or the song was stopped meanwhile: start the next one now.
			((MusicManagerAccessor) mm).musiccurator$setNextSongDelay(0);
		}
		pausedMusic = null;
	}

	// Called after SoundEngine#resume, which unpauses every channel (see SoundEngineMixin).
	public void reapplyPause() {
		if (paused && pausedMusic != null) {
			setChannelPaused(pausedMusic, true);
		}
	}

	public boolean toggleHud() {
		ModConfig c = cfg();
		c.showHud = !c.showHud;
		ConfigManager.save();
		return c.showHud;
	}

	public void applyPreset(Preset preset) {
		ModConfig c = cfg();
		c.disabledSongs = Presets.disabledFor(preset);
		c.preset = preset;
		ConfigManager.save();
		onConfigChanged();
	}

	// Applies settings edits to what is playing now. Turning the mod off resumes a paused song, since pausing
	// is a mod feature; a track that just got disabled stops instead of playing to its end.
	public void onConfigChanged() {
		if (!isEnabled()) {
			setPaused(false);
			return;
		}
		if (currentSong == null || isSongEnabled(currentSong)) {
			return;
		}
		pausedMusic = null;
		MusicManager mm = Minecraft.getInstance().getMusicManager();
		mm.stopPlaying();
		if (!paused) {
			((MusicManagerAccessor) mm).musiccurator$setNextSongDelay(0);
		}
	}

	// Mirrors the track actually playing or paused, so the HUD shows its real title.
	public void clientTick() {
		Minecraft minecraft = Minecraft.getInstance();
		SoundInstance current = ((MusicManagerAccessor) minecraft.getMusicManager()).musiccurator$getCurrentMusic();
		Sound sound = current == null ? null : current.getSound();
		// isActive() leaves out instances that never started, such as SongSelector's silence marker,
		// which MusicManager keeps as currentMusic until its next tick.
		if (sound == null || !minecraft.getSoundManager().isActive(current)) {
			currentSong = null;
			return;
		}
		currentSong = sound.getLocation();
	}

	private static void setChannelPaused(SoundInstance music, boolean pause) {
		SoundEngine engine = ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager()).musiccurator$getSoundEngine();
		ChannelAccess.ChannelHandle channel = ((SoundEngineAccessor) engine).musiccurator$getInstanceToChannel().get(music);
		if (channel != null) {
			channel.execute(pause ? Channel::pause : Channel::unpause);
		}
	}
}
