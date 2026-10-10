package com.musiccurator.gametest;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

// Loads hand-written files from the game test's own run directory, then restores the defaults.
public class ConfigFileTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try {
			context.runOnClient(client -> {
				Path file = FabricLoader.getInstance().getConfigDir().resolve("music-curator.json");
				Path broken = file.resolveSibling("music-curator.json.broken");
				try {
					unparsableFileIsKeptAside(file, broken);
					storedPresetFollowsItsRule(file);
					olderFilesGainNewKeys(file);
				} finally {
					Files.deleteIfExists(broken);
					Files.deleteIfExists(file);
					ConfigManager.load();
				}
			});
		} catch (IOException e) {
			throw new AssertionError("Could not set up the config file", e);
		}
	}

	private static void unparsableFileIsKeptAside(Path file, Path broken) throws IOException {
		String typo = """
				{ "preset": "CUSTOM", "disabledSongs": ["music/game/sweden" }
				""";
		Files.writeString(file, typo);
		ConfigManager.load();
		Check.that(Files.exists(broken) && Files.readString(broken).equals(typo), "The unparsable file was not kept aside");
		ModConfig cfg = ConfigManager.get();
		Check.that(cfg.preset == Preset.ALL_VANILLA && cfg.disabledSongs.isEmpty(), "Defaults were not loaded");
	}

	// The stored list predates a tracks.json fix: the preset's rule must win over it.
	private static void storedPresetFollowsItsRule(Path file) throws IOException {
		Files.writeString(file, """
				{ "preset": "C418_ONLY", "disabledSongs": ["music/game/water/axolotl"] }
				""");
		ConfigManager.load();
		Check.that(ConfigManager.get().disabledSongs.equals(Presets.disabledFor(Preset.C418_ONLY)),
				"A stored preset selection was not re-derived from its rule");
	}

	// A file written before favorites existed.
	private static void olderFilesGainNewKeys(Path file) throws IOException {
		Files.writeString(file, """
				{ "preset": "CUSTOM", "disabledSongs": ["music/game/sweden"] }
				""");
		ConfigManager.load();
		ModConfig cfg = ConfigManager.get();
		Check.that(cfg.favoriteSongs != null && cfg.favoriteSongs.isEmpty(), "favoriteSongs was not defaulted");
		Check.that(cfg.disabledSongs.equals(Set.of("music/game/sweden")), "A CUSTOM selection was not kept as stored");
		Check.that(Files.readString(file).contains("favoriteSongs"), "The file was not rewritten with the new key");
	}
}
