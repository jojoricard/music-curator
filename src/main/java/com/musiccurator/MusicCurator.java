package com.musiccurator;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.TrackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Code shared by both loaders starts here; each loader's entrypoint (MusicCuratorFabric,
// MusicCuratorNeoForge) calls init() first. The mod is client-only.
public final class MusicCurator {
	public static final String MOD_ID = "musiccurator";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private MusicCurator() {
	}

	// Loads the data every other class reads.
	public static void init() {
		// Order matters: ConfigManager re-derives preset selections from the track catalogue.
		TrackRegistry.load();
		ConfigManager.load();
	}
}
