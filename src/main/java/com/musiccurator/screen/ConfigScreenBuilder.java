package com.musiccurator.screen;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.music.MusicController;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Cloth Config screen with General / HUD / Tracks / Favorites categories. Simple settings write straight
// to the live ModConfig on save; the preset and the per-track toggles go through drafts reconciled in the
// saving runnable, which Cloth runs after every entry's save consumer.
public final class ConfigScreenBuilder {
	// Composers in the order they joined the soundtrack; a composer missing here is listed after them.
	private static final List<String> COMPOSER_ORDER =
			List.of("C418", "Lena Raine", "Kumi Tanioka", "Aaron Cherof", "Amos Roddy", "fingerspit");

	private ConfigScreenBuilder() {
	}

	// parent: the screen to return to when closing, or null to return to the game.
	public static Screen build(@Nullable Screen parent) {
		ModConfig cfg = ConfigManager.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("musiccurator.config.title"));
		ConfigEntryBuilder eb = builder.entryBuilder();

		// Drafts per track file: enabled (resolved against the preset on save) and favorite.
		final Map<String, Boolean> draftEnabled = new LinkedHashMap<>();
		final Map<String, Boolean> draftFavorite = new LinkedHashMap<>();
		for (Track t : TrackRegistry.all()) {
			draftEnabled.put(t.file(), !cfg.disabledSongs.contains(t.file()));
			draftFavorite.put(t.file(), cfg.favoriteSongs.contains(t.file()));
		}
		final Preset originalPreset = cfg.preset;
		final Preset[] draftPreset = {cfg.preset};

		buildGeneral(builder, eb, cfg, draftPreset);
		buildHud(builder, eb, cfg);

		ConfigCategory tracks = builder.getOrCreateCategory(Component.translatable("musiccurator.config.category.tracks"));
		addTrackToggles(tracks, eb, draftEnabled, true);

		ConfigCategory favorites = builder.getOrCreateCategory(Component.translatable("musiccurator.config.category.favorites"));
		favorites.addEntry(eb.startTextDescription(Component.translatable("musiccurator.config.favorites.description")).build());
		addTrackToggles(favorites, eb, draftFavorite, false);

		builder.setSavingRunnable(() -> {
			// A newly picked preset wins over track toggles edited in the same session, so picking a preset
			// always replaces a custom selection. Picking CUSTOM itself changes nothing.
			boolean presetChanged = draftPreset[0] != originalPreset && draftPreset[0] != Preset.CUSTOM;
			if (presetChanged) {
				cfg.preset = draftPreset[0];
				cfg.disabledSongs = Presets.disabledFor(draftPreset[0]);
			} else {
				cfg.disabledSongs = filesSetTo(draftEnabled, false);
				cfg.preset = Presets.detect(cfg.disabledSongs);
			}
			cfg.favoriteSongs = filesSetTo(draftFavorite, true);
			ConfigManager.save();
			MusicController.getInstance().onConfigChanged();
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
				.setTooltip(Component.translatable("musiccurator.config.preset.tooltip"))
				.setEnumNameProvider(e -> Component.translatable(((Preset) e).translationKey()))
				.setSaveConsumer(v -> draftPreset[0] = v)
				.build());

		general.addEntry(eb.startIntSlider(Component.translatable("musiccurator.config.min_delay"), cfg.minDelaySeconds, 10, 600)
				.setDefaultValue(60)
				.setTooltip(Component.translatable("musiccurator.config.delay.tooltip"))
				.setTextGetter(v -> Component.translatable("musiccurator.config.seconds", v))
				.setSaveConsumer(v -> cfg.minDelaySeconds = v)
				.build());

		general.addEntry(eb.startIntSlider(Component.translatable("musiccurator.config.max_delay"), cfg.maxDelaySeconds, 10, 1800)
				.setDefaultValue(300)
				.setTooltip(Component.translatable("musiccurator.config.delay.tooltip"))
				.setTextGetter(v -> Component.translatable("musiccurator.config.seconds", v))
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

	private static void addTrackToggles(ConfigCategory category, ConfigEntryBuilder eb, Map<String, Boolean> draft,
			boolean defaultValue) {
		Map<String, List<Track>> byComposer = new LinkedHashMap<>();
		for (String composer : COMPOSER_ORDER) {
			byComposer.put(composer, new ArrayList<>());
		}
		for (Track t : TrackRegistry.all()) {
			byComposer.computeIfAbsent(t.composer(), k -> new ArrayList<>()).add(t);
		}

		for (Map.Entry<String, List<Track>> group : byComposer.entrySet()) {
			List<Track> songs = group.getValue();
			if (songs.isEmpty()) {
				continue;
			}
			songs.sort(Comparator.comparing(Track::title, String.CASE_INSENSITIVE_ORDER));
			List<AbstractConfigListEntry> entries = new ArrayList<>();
			for (Track t : songs) {
				entries.add(eb.startBooleanToggle(Component.literal(t.title()), draft.get(t.file()))
						.setDefaultValue(defaultValue)
						.setSaveConsumer(v -> draft.put(t.file(), v))
						.build());
			}
			category.addEntry(eb.startSubCategory(Component.literal(group.getKey() + " (" + songs.size() + ")"), entries)
					.setExpanded(false)
					.build());
		}
	}

	private static Set<String> filesSetTo(Map<String, Boolean> draft, boolean value) {
		Set<String> files = new LinkedHashSet<>();
		for (Map.Entry<String, Boolean> e : draft.entrySet()) {
			if (Boolean.valueOf(value).equals(e.getValue())) {
				files.add(e.getKey());
			}
		}
		return files;
	}
}
