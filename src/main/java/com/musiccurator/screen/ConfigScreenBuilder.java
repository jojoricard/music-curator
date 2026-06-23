package com.musiccurator.screen;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ConfigScreenBuilder {
	private ConfigScreenBuilder() {
	}

	static Screen build(Screen parent) {
		ModConfig cfg = ConfigManager.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("musiccurator.config.title"));
		ConfigEntryBuilder eb = builder.entryBuilder();

		// Draft = which songs are enabled (true). Resolved against the preset on save.
		final Map<String, Boolean> draftSongs = new LinkedHashMap<>();
		for (Track t : TrackRegistry.all()) {
			draftSongs.put(t.file(), !cfg.disabledSongs.contains(t.file()));
		}
		final Preset originalPreset = cfg.preset;
		final Preset[] draftPreset = {cfg.preset};

		buildGeneral(builder, eb, cfg, draftPreset);
		buildHud(builder, eb, cfg);
		buildTracks(builder, eb, draftSongs);

		builder.setSavingRunnable(() -> {
			boolean presetChanged = draftPreset[0] != originalPreset && draftPreset[0] != Preset.CUSTOM;
			if (presetChanged) {
				cfg.preset = draftPreset[0];
				cfg.disabledSongs = Presets.disabledFor(draftPreset[0]);
			} else {
				Set<String> disabled = new LinkedHashSet<>();
				for (Map.Entry<String, Boolean> e : draftSongs.entrySet()) {
					if (!Boolean.TRUE.equals(e.getValue())) {
						disabled.add(e.getKey());
					}
				}
				cfg.disabledSongs = disabled;
				cfg.preset = Presets.detect(disabled);
			}
			ConfigManager.save();
		});

		return builder.build();
	}

	private static void buildGeneral(ConfigBuilder builder, ConfigEntryBuilder eb, ModConfig cfg, Preset[] draftPreset) {
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("musiccurator.config.category.general"));

		general.addEntry(eb.startBooleanToggle(Component.translatable("musiccurator.config.enabled"), cfg.enabled)
				.setDefaultValue(true)
				.setTooltip(Component.translatable("musiccurator.config.enabled.tooltip"))
				.setSaveConsumer(v -> cfg.enabled = v)
				.build());

		general.addEntry(eb.startEnumSelector(Component.translatable("musiccurator.config.preset"), Preset.class, cfg.preset)
				.setDefaultValue(Preset.ALL_VANILLA)
				.setEnumNameProvider(e -> Component.translatable(((Preset) e).translationKey()))
				.setSaveConsumer(v -> draftPreset[0] = v)
				.build());

		general.addEntry(eb.startIntSlider(Component.translatable("musiccurator.config.min_delay"), cfg.minDelaySeconds, 10, 600)
				.setDefaultValue(60)
				.setTextGetter(v -> Component.literal(v + "s"))
				.setSaveConsumer(v -> cfg.minDelaySeconds = v)
				.build());

		general.addEntry(eb.startIntSlider(Component.translatable("musiccurator.config.max_delay"), cfg.maxDelaySeconds, 10, 1800)
				.setDefaultValue(300)
				.setTextGetter(v -> Component.literal(v + "s"))
				.setSaveConsumer(v -> cfg.maxDelaySeconds = v)
				.build());

		general.addEntry(eb.startBooleanToggle(Component.translatable("musiccurator.config.override_delay"), cfg.overrideVanillaDelay)
				.setDefaultValue(false)
				.setTooltip(Component.translatable("musiccurator.config.override_delay.tooltip"))
				.setSaveConsumer(v -> cfg.overrideVanillaDelay = v)
				.build());
	}

	private static void buildHud(ConfigBuilder builder, ConfigEntryBuilder eb, ModConfig cfg) {
		ConfigCategory hud = builder.getOrCreateCategory(Component.translatable("musiccurator.config.category.hud"));

		hud.addEntry(eb.startBooleanToggle(Component.translatable("musiccurator.config.hud.show"), cfg.showHud)
				.setDefaultValue(true)
				.setSaveConsumer(v -> cfg.showHud = v)
				.build());

		hud.addEntry(eb.startEnumSelector(Component.translatable("musiccurator.config.hud.anchor"), ModConfig.HudAnchor.class, cfg.hudAnchor)
				.setDefaultValue(ModConfig.HudAnchor.TOP_RIGHT)
				.setEnumNameProvider(e -> Component.translatable(((ModConfig.HudAnchor) e).translationKey()))
				.setSaveConsumer(v -> cfg.hudAnchor = v)
				.build());

		hud.addEntry(eb.startIntField(Component.translatable("musiccurator.config.hud.offset_x"), cfg.hudOffsetX)
				.setDefaultValue(4)
				.setSaveConsumer(v -> cfg.hudOffsetX = v)
				.build());

		hud.addEntry(eb.startIntField(Component.translatable("musiccurator.config.hud.offset_y"), cfg.hudOffsetY)
				.setDefaultValue(4)
				.setSaveConsumer(v -> cfg.hudOffsetY = v)
				.build());

		hud.addEntry(eb.startBooleanToggle(Component.translatable("musiccurator.config.hud.show_composer"), cfg.hudShowComposer)
				.setDefaultValue(true)
				.setSaveConsumer(v -> cfg.hudShowComposer = v)
				.build());

		hud.addEntry(eb.startBooleanToggle(Component.translatable("musiccurator.config.hud.show_category"), cfg.hudShowCategory)
				.setDefaultValue(false)
				.setSaveConsumer(v -> cfg.hudShowCategory = v)
				.build());
	}

	private static void buildTracks(ConfigBuilder builder, ConfigEntryBuilder eb, Map<String, Boolean> draftSongs) {
		ConfigCategory tracks = builder.getOrCreateCategory(Component.translatable("musiccurator.config.category.tracks"));

		// Group songs by composer, with a sensible ordering.
		Map<String, List<Track>> grouped = new LinkedHashMap<>();
		for (String composer : List.of("C418", "Lena Raine", "Kumi Tanioka", "Aaron Cherof")) {
			grouped.put(composer, new ArrayList<>());
		}
		for (Track t : TrackRegistry.all()) {
			String key = (t.composer() == null || t.composer().isEmpty()) ? "Other" : t.composer();
			grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
		}

		for (Map.Entry<String, List<Track>> group : grouped.entrySet()) {
			if (group.getValue().isEmpty()) {
				continue;
			}
			List<AbstractConfigListEntry> entries = new ArrayList<>();
			for (Track t : group.getValue()) {
				entries.add(eb.startBooleanToggle(Component.literal(t.title()), draftSongs.get(t.file()))
						.setDefaultValue(true)
						.setSaveConsumer(v -> draftSongs.put(t.file(), v))
						.build());
			}
			tracks.addEntry(eb.startSubCategory(Component.literal(group.getKey() + " (" + group.getValue().size() + ")"), entries)
					.setExpanded(false)
					.build());
		}
	}
}
