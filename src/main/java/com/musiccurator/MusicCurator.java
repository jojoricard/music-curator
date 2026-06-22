package com.musiccurator;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.TrackRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicCurator implements ModInitializer {
	public static final String MOD_ID = "musiccurator";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TrackRegistry.load();
		ConfigManager.load();
	}
}
