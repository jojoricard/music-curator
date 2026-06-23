package com.musiccurator.config;

import java.util.LinkedHashSet;
import java.util.Set;

// Serialized to config/music-curator.json by Gson.
public class ModConfig {
	public boolean enabled = true;
	public Preset preset = Preset.ALL_VANILLA;
	// Song files (e.g. "music/game/sweden") the user has turned off. Empty = everything plays.
	public Set<String> disabledSongs = new LinkedHashSet<>();

	public int minDelaySeconds = 60;
	public int maxDelaySeconds = 300;
	public boolean overrideVanillaDelay = false;

	public boolean showHud = true;
	public HudAnchor hudAnchor = HudAnchor.TOP_RIGHT;
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
