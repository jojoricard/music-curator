package com.musiccurator.config;

import java.util.LinkedHashSet;
import java.util.Set;

// Persisted settings, serialized by Gson to config/music-curator.json. Field names are the JSON keys:
// renaming one silently resets that setting for every existing player.
public class ModConfig {
	public boolean enabled = true;
	// When not CUSTOM, ConfigManager re-derives disabledSongs from it at every load.
	public Preset preset = Preset.ALL_VANILLA;
	// Track files (e.g. "music/game/sweden") that must not play. Anything not listed plays, including
	// tracks missing from tracks.json.
	public Set<String> disabledSongs = new LinkedHashSet<>();
	// Track files drawn more often (see SongSelector). Independent of disabledSongs and of presets, so a
	// favorite that a preset disables is a favorite again once re-enabled.
	public Set<String> favoriteSongs = new LinkedHashSet<>();

	public int minDelaySeconds = 60;
	public int maxDelaySeconds = 300;
	// Means "fixed interval: always wait exactly minDelaySeconds". The misleading name is kept because
	// it is the key in existing config files.
	public boolean overrideVanillaDelay = false;

	public boolean showHud = true;
	public HudAnchor hudAnchor = HudAnchor.TOP_RIGHT;
	// Distance from the anchored corner, in GUI-scaled pixels.
	public int hudOffsetX = 4;
	public int hudOffsetY = 4;
	public boolean hudShowComposer = true;
	public boolean hudShowCategory = false;

	public enum HudAnchor {
		TOP_LEFT("musiccurator.anchor.top_left"),
		TOP_RIGHT("musiccurator.anchor.top_right"),
		BOTTOM_LEFT("musiccurator.anchor.bottom_left"),
		BOTTOM_RIGHT("musiccurator.anchor.bottom_right");

		private final String translationKey;

		HudAnchor(String translationKey) {
			this.translationKey = translationKey;
		}

		public String translationKey() {
			return translationKey;
		}
	}
}
