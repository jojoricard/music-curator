package com.musiccurator.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Cumulative version presets: each step keeps everything from the steps before it.
public final class Presets {
	// Ordered "eras" of music. A track's version maps to an index here.
	private static final List<String> VERSION_ORDER = List.of(
			"1.0", "1.9", "1.13", "1.16", "1.18", "1.19", "1.20", "1.21", "26.2"
	);

	private Presets() {
	}

	public static Set<String> tracksFor(Preset preset) {
		Set<String> out = new LinkedHashSet<>();
		for (Track t : TrackRegistry.all()) {
			if (matches(preset, t)) {
				out.add(t.id());
			}
		}
		return out;
	}

	// Returns the preset whose set matches the given selection, or CUSTOM if none do.
	public static Preset detect(Set<String> selection) {
		Preset[] candidates = {
				Preset.ALL_VANILLA, Preset.C418_ONLY, Preset.NETHER_1_16,
				Preset.CAVES_CLIFFS_1_18, Preset.WILD_1_19, Preset.LATEST
		};
		for (Preset p : candidates) {
			if (tracksFor(p).equals(selection)) {
				return p;
			}
		}
		return Preset.CUSTOM;
	}

	private static boolean matches(Preset preset, Track t) {
		return switch (preset) {
			case ALL_VANILLA, LATEST, CUSTOM -> true;
			case C418_ONLY -> isC418(t);
			case NETHER_1_16 -> isC418(t) || versionAtMost(t, "1.16");
			case CAVES_CLIFFS_1_18 -> isC418(t) || versionAtMost(t, "1.18");
			case WILD_1_19 -> isC418(t) || versionAtMost(t, "1.19");
		};
	}

	private static boolean isC418(Track t) {
		return "C418".equalsIgnoreCase(t.composer());
	}

	private static boolean versionAtMost(Track t, String cap) {
		int trackIdx = VERSION_ORDER.indexOf(t.version());
		int capIdx = VERSION_ORDER.indexOf(cap);
		// Unknown versions sort last, so they only show up in the all-inclusive presets.
		if (trackIdx < 0) {
			return false;
		}
		return trackIdx <= capIdx;
	}
}
