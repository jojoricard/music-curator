package com.musiccurator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.musiccurator.MusicCurator;
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else {
/*import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;

// Owns config/music-curator.json. get() returns the live instance: the config screen, commands and
// keybinds edit it in place, then call save().
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "music-curator.json";
	private static final String BROKEN_FILE_NAME = FILE_NAME + ".broken";
	//? if fabric {
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	//?} else {
	/*private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
	*///?}

	private static ModConfig config;

	private ConfigManager() {
	}

	public static ModConfig get() {
		if (config == null) {
			load();
		}
		return config;
	}

	// Requires TrackRegistry to be loaded: preset selections are re-derived from the catalogue.
	public static void load() {
		ModConfig loaded = null;
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				loaded = GSON.fromJson(reader, ModConfig.class);
			} catch (JsonParseException e) {
				// Typically a typo from a hand edit. Moving the file aside keeps the player's selection
				// recoverable instead of overwriting it with defaults on the save() below.
				MusicCurator.LOGGER.error("Could not parse {}; renamed it to {} and loaded defaults",
						FILE_NAME, BROKEN_FILE_NAME, e);
				moveAsideBrokenFile();
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to read " + FILE_NAME, e);
			}
		}

		// Gson returns null for an empty file.
		if (loaded == null) {
			loaded = new ModConfig();
		}
		// Keys missing from older files keep their ModConfig defaults, but an explicit null or an unknown
		// enum name deserializes to null.
		if (loaded.disabledSongs == null) {
			loaded.disabledSongs = new LinkedHashSet<>();
		}
		if (loaded.favoriteSongs == null) {
			loaded.favoriteSongs = new LinkedHashSet<>();
		}
		if (loaded.preset == null) {
			loaded.preset = Preset.ALL_VANILLA;
		}
		if (loaded.hudAnchor == null) {
			loaded.hudAnchor = ModConfig.HudAnchor.TOP_RIGHT;
		}
		// A preset is a rule, not a snapshot: re-deriving it picks up tracks.json changes between releases
		// (new tracks, corrected composers) that a stored list would miss.
		if (loaded.preset != Preset.CUSTOM) {
			loaded.disabledSongs = Presets.disabledFor(loaded.preset);
		}

		config = loaded;
		// Written back at once so the file exists and lists every current key for hand editing.
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
			// Not fatal: the settings stay in effect for this session.
			MusicCurator.LOGGER.error("Failed to write {}", FILE_NAME, e);
		}
	}

	private static void moveAsideBrokenFile() {
		try {
			Files.move(PATH, PATH.resolveSibling(BROKEN_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not rename unparsable " + FILE_NAME + " to " + BROKEN_FILE_NAME, e);
		}
	}
}
