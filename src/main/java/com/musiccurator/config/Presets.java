package com.musiccurator.config;

import java.util.LinkedHashSet;
import java.util.Set;

// Presets are expressed as the set of song files they DISABLE.
// ALL_VANILLA disables nothing; selection is otherwise per-song.
public final class Presets {
	private Presets() {
	}

	public static Set<String> disabledFor(Preset preset) {
		Set<String> out = new LinkedHashSet<>();
		for (Track t : TrackRegistry.all()) {
			boolean c418 = "C418".equalsIgnoreCase(t.composer());
			switch (preset) {
				case ALL_VANILLA, CUSTOM -> {
					// nothing disabled
				}
				case C418_ONLY -> {
					if (!c418) {
						out.add(t.file());
					}
				}
				case MODERN_ONLY -> {
					if (c418) {
						out.add(t.file());
					}
				}
			}
		}
		return out;
	}

	// Returns the preset whose disabled-set matches the selection, or CUSTOM.
	public static Preset detect(Set<String> disabled) {
		Preset[] candidates = {Preset.ALL_VANILLA, Preset.C418_ONLY, Preset.MODERN_ONLY};
		for (Preset p : candidates) {
			if (disabledFor(p).equals(disabled)) {
				return p;
			}
		}
		return Preset.CUSTOM;
	}
}
