package com.musiccurator.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Read-only catalogue of the vanilla music tracks, loaded once from the bundled tracks.json.
// Titles and composers copy the game's own "Now Playing" names (vanilla en_us.json keys such as
// "music.game.sweden"): refresh both from there when Minecraft adds music.
public final class TrackRegistry {
	private static final String RESOURCE = "/musiccurator/tracks.json";
	private static final Gson GSON = new Gson();
	// Each jar is built for one Minecraft version, which Stonecutter writes here.
	private static final String GAME_VERSION = /*$ minecraft*/ "26.2";

	private static List<Track> tracks = List.of();
	private static Map<String, Track> byFile = Map.of();

	private TrackRegistry() {
	}

	// Fails fast: a missing or malformed catalogue is a packaging bug, and running without it would turn
	// every track into an unknown one that can be neither listed nor disabled.
	public static void load() {
		List<Track> parsed;
		try (InputStream in = TrackRegistry.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				throw new IllegalStateException("Missing bundled resource " + RESOURCE);
			}
			parsed = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), new TypeToken<List<Track>>() {
			});
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read " + RESOURCE, e);
		}
		if (parsed == null || parsed.isEmpty()) {
			throw new IllegalStateException(RESOURCE + " lists no tracks");
		}

		// One catalogue serves every supported Minecraft version: tracks newer than the running game are left out.
		int[] game = releaseNumbers(GAME_VERSION);
		Set<String> files = new HashSet<>();
		List<Track> available = new ArrayList<>();
		for (Track track : parsed) {
			if (track == null || isBlank(track.file()) || isBlank(track.title())
					|| isBlank(track.composer()) || isBlank(track.category())) {
				throw new IllegalStateException("Incomplete entry in " + RESOURCE + ": " + track);
			}
			if (!files.add(track.file())) {
				throw new IllegalStateException("Duplicate entry in " + RESOURCE + ": " + track.file());
			}
			if (track.since() == null || Arrays.compare(game, since(track)) >= 0) {
				available.add(track);
			}
		}

		Map<String, Track> index = new HashMap<>();
		for (Track track : available) {
			index.put(track.file(), track);
		}
		tracks = List.copyOf(available);
		byFile = Map.copyOf(index);
	}

	private static int[] since(Track track) {
		try {
			return releaseNumbers(track.since());
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Invalid 'since' version in " + RESOURCE + ": " + track, e);
		}
	}

	// "1.21.11" -> [1, 21, 11], which Arrays.compare orders like release numbers (1.21 < 1.21.1 < 1.21.11 < 26.2).
	private static int[] releaseNumbers(String version) {
		return Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
	}

	public static List<Track> all() {
		return tracks;
	}

	// Null for sounds outside the catalogue, e.g. music added by a resource pack.
	@Nullable
	public static Track get(String file) {
		return byFile.get(file);
	}

	// Tracks outside the catalogue get a name derived from their file: "music/game/foo_bar" -> "Foo Bar".
	public static String titleOf(String file) {
		Track track = byFile.get(file);
		if (track != null) {
			return track.title();
		}
		String base = file.substring(file.lastIndexOf('/') + 1).replace('_', ' ');
		StringBuilder sb = new StringBuilder(base.length());
		boolean cap = true;
		for (char c : base.toCharArray()) {
			sb.append(cap ? Character.toUpperCase(c) : c);
			cap = c == ' ';
		}
		return sb.toString();
	}

	private static boolean isBlank(@Nullable String value) {
		return value == null || value.isBlank();
	}
}
