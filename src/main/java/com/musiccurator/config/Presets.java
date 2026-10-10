package com.musiccurator.config;

import java.util.LinkedHashSet;
import java.util.Set;

// Preset rules, expressed as the set of track files each preset disables.
public final class Presets {
	private Presets() {
	}

	public static Set<String> disabledFor(Preset preset) {
		Set<String> disabled = new LinkedHashSet<>();
		for (Track t : TrackRegistry.all()) {
			boolean c418 = "C418".equalsIgnoreCase(t.composer());
			// A switch expression, so adding a Preset without a rule fails to compile.
			boolean plays = switch (preset) {
				case ALL_VANILLA -> true;
				case C418_ONLY -> c418;
				case MODERN_ONLY -> !c418;
				// Answering "nothing disabled" would wipe the player's hand-picked selection.
				case CUSTOM -> throw new IllegalArgumentException("CUSTOM has no rule to apply");
			};
			if (!plays) {
				disabled.add(t.file());
			}
		}
		return disabled;
	}

	// Reverse of disabledFor: the preset whose rule yields exactly this selection, or CUSTOM.
	public static Preset detect(Set<String> disabled) {
		for (Preset p : Preset.values()) {
			if (p != Preset.CUSTOM && disabledFor(p).equals(disabled)) {
				return p;
			}
		}
		return Preset.CUSTOM;
	}
}
