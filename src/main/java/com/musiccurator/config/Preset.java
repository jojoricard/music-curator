package com.musiccurator.config;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

// Single source of truth for which presets exist; their track rules live in Presets.
public enum Preset {
	ALL_VANILLA("musiccurator.preset.all_vanilla", "all"),
	C418_ONLY("musiccurator.preset.c418_only", "c418"),
	MODERN_ONLY("musiccurator.preset.modern_only", "modern"),
	// Not a rule but the label of a hand-picked selection: it is detected, never applied.
	CUSTOM("musiccurator.preset.custom", "custom");

	private final String translationKey;
	// Argument of "/musiccurator preset <alias>".
	private final String alias;

	Preset(String translationKey, String alias) {
		this.translationKey = translationKey;
		this.alias = alias;
	}

	public String translationKey() {
		return translationKey;
	}

	public String alias() {
		return alias;
	}

	@Nullable
	public static Preset byAlias(String name) {
		// Locale.ROOT: the default locale's rules can change letters (e.g. Turkish dotless i).
		String wanted = name.toLowerCase(Locale.ROOT);
		for (Preset p : values()) {
			if (p.alias.equals(wanted)) {
				return p;
			}
		}
		return null;
	}
}
