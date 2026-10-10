package com.musiccurator.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Read-only catalogue of the vanilla music tracks, loaded once from the bundled tracks.json.
// Titles and composers copy the game's own "Now Playing" names (vanilla en_us.json keys such as
// "music.game.sweden"): refresh both from there when Minecraft adds music.
public final class TrackRegistry {
	private static final String RESOURCE = "/musiccurator/tracks.json";
	private static final Gson GSON = new Gson();

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

		Map<String, Track> index = new HashMap<>();
		for (Track track : parsed) {
			if (track == null || isBlank(track.file()) || isBlank(track.title())
					|| isBlank(track.composer()) || isBlank(track.category())) {
				throw new IllegalStateException("Incomplete entry in " + RESOURCE + ": " + track);
			}
			if (index.putIfAbsent(track.file(), track) != null) {
				throw new IllegalStateException("Duplicate entry in " + RESOURCE + ": " + track.file());
			}
		}
		tracks = List.copyOf(parsed);
		byFile = Map.copyOf(index);
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
