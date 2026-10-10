package com.musiccurator.gametest;

import com.musiccurator.config.Preset;
import com.musiccurator.config.Presets;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.mixin.WeighedSoundEventsAccessor;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;

import java.util.Set;
import java.util.TreeSet;

// tracks.json is hand-maintained: it must list exactly the files the game's music events play, under the
// game's own "Now Playing" names, since the presets are computed from its composers.
public class TrackCatalogueTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(client -> {
			namesMatchTheNowPlayingToast();
			catalogueListsExactlyTheMusicFiles(client);
			presetsFollowComposers();
		});
	}

	private static void namesMatchTheNowPlayingToast() {
		Language language = Language.getInstance();
		for (Track track : TrackRegistry.all()) {
			String inGame = language.getOrDefault(track.file().replace('/', '.'));
			String inCatalogue = track.composer() + " - " + track.title();
			Check.that(inCatalogue.equals(inGame), "%s is '%s' in tracks.json but '%s' in the game",
					track.file(), inCatalogue, inGame);
		}
	}

	private static void catalogueListsExactlyTheMusicFiles(Minecraft client) {
		SoundManager soundManager = client.getSoundManager();
		Set<String> played = new TreeSet<>();
		for (Identifier id : soundManager.getAvailableSounds()) {
			if (!id.getPath().startsWith("music.")) {
				continue;
			}
			for (Weighted<Sound> entry : ((WeighedSoundEventsAccessor) (Object) soundManager.getSoundEvent(id)).musiccurator$getList()) {
				if (entry instanceof Sound sound) {
					played.add(sound.getLocation().getPath());
				}
			}
		}
		Set<String> catalogued = new TreeSet<>();
		for (Track track : TrackRegistry.all()) {
			catalogued.add(track.file());
		}

		Set<String> missing = new TreeSet<>(played);
		missing.removeAll(catalogued);
		Set<String> stale = new TreeSet<>(catalogued);
		stale.removeAll(played);
		Check.that(missing.isEmpty(), "Music files missing from tracks.json: %s", missing);
		Check.that(stale.isEmpty(), "tracks.json lists files that no music event plays: %s", stale);
	}

	private static void presetsFollowComposers() {
		Set<String> c418Only = Presets.disabledFor(Preset.C418_ONLY);
		Set<String> modernOnly = Presets.disabledFor(Preset.MODERN_ONLY);
		for (Track track : TrackRegistry.all()) {
			boolean byC418 = track.composer().equals("C418");
			Check.that(c418Only.contains(track.file()) != byC418, "'C418 only' gets %s wrong", track.file());
			Check.that(modernOnly.contains(track.file()) == byC418, "'Modern only' gets %s wrong", track.file());
		}
		Check.that(Presets.disabledFor(Preset.ALL_VANILLA).isEmpty(), "'All vanilla' disables tracks");

		for (Preset preset : Preset.values()) {
			if (preset != Preset.CUSTOM) {
				Check.that(Presets.detect(Presets.disabledFor(preset)) == preset, "detect() does not recognise %s", preset);
			}
		}
		Check.that(Presets.detect(Set.of("music/game/sweden")) == Preset.CUSTOM, "A hand-picked selection is not CUSTOM");

		try {
			Presets.disabledFor(Preset.CUSTOM);
			throw new AssertionError("disabledFor(CUSTOM) must refuse: CUSTOM has no rule");
		} catch (IllegalArgumentException expected) {
			// CUSTOM is a label, not a rule.
		}
	}
}
