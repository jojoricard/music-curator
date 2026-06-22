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

		// Preset and track checkboxes are resolved together on save (see savingRunnable).
		final Map<String, Boolean> draftTracks = new LinkedHashMap<>();
		for (Track t : TrackRegistry.all()) {
			draftTracks.put(t.id(), cfg.enabledTracks.contains(t.id()));
		}
		final Preset originalPreset = cfg.preset;
		final Preset[] draftPreset = {cfg.preset};

		buildGeneral(builder, eb, cfg, draftPreset);
		buildHud(builder, eb, cfg);
		buildTracks(builder, eb, draftTracks);

		builder.setSavingRunnable(() -> {
			boolean presetChanged = draftPreset[0] != originalPreset && draftPreset[0] != Preset.CUSTOM;
			if (presetChanged) {
				cfg.preset = draftPreset[0];
				cfg.enabledTracks = Presets.tracksFor(draftPreset[0]);
			} else {
				Set<String> set = new LinkedHashSet<>();
				for (Map.Entry<String, Boolean> e : draftTracks.entrySet()) {
					if (Boolean.TRUE.equals(e.getValue())) {
						set.add(e.getKey());
					}
				}
				cfg.enabledTracks = set;
				cfg.preset = Presets.detect(set);
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

	private static void buildTracks(ConfigBuilder builder, ConfigEntryBuilder eb, Map<String, Boolean> draftTracks) {
		ConfigCategory tracks = builder.getOrCreateCategory(Component.translatable("musiccurator.config.category.tracks"));

		Map<String, List<Track>> grouped = new LinkedHashMap<>();
		for (Track t : TrackRegistry.all()) {
			grouped.computeIfAbsent(t.category(), k -> new ArrayList<>()).add(t);
		}

		for (Map.Entry<String, List<Track>> group : grouped.entrySet()) {
			List<AbstractConfigListEntry> entries = new ArrayList<>();
			for (Track t : group.getValue()) {
				entries.add(eb.startBooleanToggle(Component.literal(t.name() + " (" + t.version() + ")"), draftTracks.get(t.id()))
						.setDefaultValue(true)
						.setSaveConsumer(v -> draftTracks.put(t.id(), v))
						.build());
			}
			tracks.addEntry(eb.startSubCategory(Component.literal(capitalize(group.getKey())), entries)
					.setExpanded(false)
					.build());
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
