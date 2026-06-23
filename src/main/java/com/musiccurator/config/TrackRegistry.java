package com.musiccurator.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musiccurator.MusicCurator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Reads the bundled per-track list. Each track is a single music .ogg keyed by its
// file path (e.g. "music/game/sweden"), which matches Sound#getLocation() in-game.
public final class TrackRegistry {
	private static final Gson GSON = new Gson();

	private static List<Track> tracks = List.of();
	private static Map<String, Track> byFile = Map.of();

	private TrackRegistry() {
	}

	public static void load() {
		try (InputStream in = TrackRegistry.class.getResourceAsStream("/musiccurator/tracks.json")) {
			if (in == null) {
				MusicCurator.LOGGER.warn("tracks.json not found; no tracks will be curated");
				tracks = List.of();
				byFile = Map.of();
				return;
			}
			Type type = new TypeToken<List<Track>>() {
			}.getType();
			try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				List<Track> parsed = GSON.fromJson(reader, type);
				tracks = parsed == null ? List.of() : List.copyOf(parsed);
			}
			Map<String, Track> map = new LinkedHashMap<>();
			for (Track t : tracks) {
				map.put(t.file(), t);
			}
			byFile = Collections.unmodifiableMap(map);
		} catch (Exception e) {
			MusicCurator.LOGGER.error("Failed to read tracks.json", e);
			tracks = List.of();
			byFile = Map.of();
		}
	}

	public static List<Track> all() {
		return tracks;
	}

	public static Track get(String file) {
		return byFile.get(file);
	}

	public static Set<String> allFiles() {
		return byFile.keySet();
	}
}
