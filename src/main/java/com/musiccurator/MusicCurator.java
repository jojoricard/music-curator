package com.musiccurator;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.TrackRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Loads the data every other class reads. The mod is client-only (fabric.mod.json), so this "main"
// entrypoint only ever runs on the client, before MusicCuratorClient.
public class MusicCurator implements ModInitializer {
	public static final String MOD_ID = "musiccurator";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Order matters: ConfigManager re-derives preset selections from the track catalogue.
		TrackRegistry.load();
		ConfigManager.load();
	}
}
