package com.musiccurator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.musiccurator.MusicCurator;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("music-curator.json");

	private static ModConfig config;

	private ConfigManager() {
	}

	public static ModConfig get() {
		if (config == null) {
			load();
		}
		return config;
	}

	public static void load() {
		ModConfig loaded = null;
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				loaded = GSON.fromJson(reader, ModConfig.class);
			} catch (Exception e) {
				MusicCurator.LOGGER.error("Failed to read config, using defaults", e);
			}
		}

		if (loaded == null) {
			loaded = new ModConfig();
		}
		if (loaded.enabledTracks == null) {
			loaded.enabledTracks = new LinkedHashSet<>();
		}
		if (loaded.preset == null) {
			loaded.preset = Preset.ALL_VANILLA;
		}
		if (loaded.hudAnchor == null) {
			loaded.hudAnchor = ModConfig.HudAnchor.TOP_RIGHT;
		}
		// First run (no saved selection yet): seed from the preset.
		if (loaded.enabledTracks.isEmpty() && loaded.preset != Preset.CUSTOM) {
			loaded.enabledTracks = Presets.tracksFor(loaded.preset);
		}

		config = loaded;
		save();
	}

	public static void save() {
		if (config == null) {
			return;
		}
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			MusicCurator.LOGGER.error("Failed to write config", e);
		}
	}
}
