package com.musiccurator.config;

public enum Preset {
	ALL_VANILLA("musiccurator.preset.all_vanilla", "all"),
	C418_ONLY("musiccurator.preset.c418_only", "c418"),
	MODERN_ONLY("musiccurator.preset.modern_only", "modern"),
	CUSTOM("musiccurator.preset.custom", "custom");

	private final String translationKey;
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

	public static Preset byAlias(String name) {
		String wanted = name.toLowerCase();
		for (Preset p : values()) {
			if (p.alias.equals(wanted)) {
				return p;
			}
		}
		return null;
	}
}
