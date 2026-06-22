package com.musiccurator.config;

public enum Preset {
	ALL_VANILLA("musiccurator.preset.all_vanilla", "all"),
	C418_ONLY("musiccurator.preset.c418_only", "c418"),
	NETHER_1_16("musiccurator.preset.nether_1_16", "nether"),
	CAVES_CLIFFS_1_18("musiccurator.preset.caves_cliffs_1_18", "caves"),
	WILD_1_19("musiccurator.preset.wild_1_19", "wild"),
	LATEST("musiccurator.preset.latest", "latest"),
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
