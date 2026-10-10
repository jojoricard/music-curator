package com.musiccurator.gametest;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.mixin.WeighedSoundEventsAccessor;
import com.musiccurator.music.MusicController;
import com.musiccurator.music.SongSelector;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// Draws music against the real sound registry. Counting tests call SongSelector with a fixed seed, so
// they are deterministic; tests through the game's own path (resolve()) only assert RNG-free outcomes.
public class SongSelectionTest implements FabricClientGameTest {
	private static final String SWEDEN = "music/game/sweden";
	private static final String WARMTH = "music/game/nether/warmth";

	@Override
	public void runTest(ClientGameTestContext context) {
		try {
			context.runOnClient(client -> {
				ModConfig cfg = ConfigManager.get();
				Set<String> disabled = cfg.disabledSongs;
				Set<String> favorites = cfg.favoriteSongs;
				int maxDelaySeconds = cfg.maxDelaySeconds;
				try {
					creativeDrawsFromItsNestedGamePool(client, cfg);
					disabledTracksNeverPlay(client, cfg);
					selectionStaysAudibleOutsideItsOwnPool(client, cfg);
					nothingPlaysWhenEveryTrackIsDisabled(client, cfg);
					onlyBackgroundMusicIsCurated(client, cfg);
					favoritesPlayMoreOften(client, cfg);
					timingFollowsThePlayerOnlyInWorlds(cfg);
				} finally {
					cfg.disabledSongs = disabled;
					cfg.favoriteSongs = favorites;
					cfg.maxDelaySeconds = maxDelaySeconds;
				}
			});
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("MusicManager.MusicFrequency#getNextSongDelay is no longer reachable", e);
		}
	}

	// music.creative lists its own 6 tracks plus a reference to the whole music.game event.
	private static void creativeDrawsFromItsNestedGamePool(Minecraft client, ModConfig cfg) {
		cfg.disabledSongs = new HashSet<>();
		Set<String> expected = new TreeSet<>(ownFiles(client, "music.creative"));
		expected.addAll(ownFiles(client, "music.game"));

		RandomSource random = RandomSource.create(1);
		Set<String> drawn = new TreeSet<>();
		String previous = null;
		for (int i = 0; i < 5000; i++) {
			String file = draw(client, "music.creative", random);
			Check.that(!file.equals(previous), "%s played twice in a row", file);
			drawn.add(file);
			previous = file;
		}
		Check.that(drawn.equals(expected), "Creative drew %d tracks, expected the %d of music.creative and music.game",
				drawn.size(), expected.size());

		// The game's own path: only Sweden is enabled and it lives in the nested music.game event.
		cfg.disabledSongs = allFilesExcept(SWEDEN);
		for (int i = 0; i < 20; i++) {
			Check.that(resolve(client, "music.creative", true).equals(SWEDEN), "resolve() did not go through SongSelector");
		}
	}

	private static void disabledTracksNeverPlay(Minecraft client, ModConfig cfg) {
		cfg.disabledSongs = ownFiles(client, "music.creative");
		RandomSource random = RandomSource.create(2);
		Set<String> drawn = new TreeSet<>();
		for (int i = 0; i < 2000; i++) {
			drawn.add(draw(client, "music.creative", random));
		}
		Check.that(drawn.equals(ownFiles(client, "music.game")), "With its own tracks disabled, Creative drew %s", drawn);
	}

	// Warmth only plays in the Nether: in the overworld pool, the selection must still be heard.
	private static void selectionStaysAudibleOutsideItsOwnPool(Minecraft client, ModConfig cfg) {
		cfg.disabledSongs = allFilesExcept(WARMTH);
		RandomSource random = RandomSource.create(3);
		for (int i = 0; i < 100; i++) {
			String file = draw(client, "music.game", random);
			Check.that(file.equals(WARMTH), "Expected the only enabled track, got %s", file);
		}
	}

	private static void nothingPlaysWhenEveryTrackIsDisabled(Minecraft client, ModConfig cfg) {
		cfg.disabledSongs = allFilesExcept();
		Sound sound = SongSelector.choose(event(client, "music.game"), RandomSource.create(4), SoundManager.EMPTY_SOUND);
		Check.that(sound == SoundManager.INTENTIONALLY_EMPTY_SOUND, "Expected silence, got %s", sound.getLocation());
	}

	// Jukebox discs and other sounds keep vanilla's draw, even from a music event.
	private static void onlyBackgroundMusicIsCurated(Minecraft client, ModConfig cfg) {
		cfg.disabledSongs = allFilesExcept(SWEDEN);
		boolean vanillaDrawSeen = false;
		for (int i = 0; i < 100 && !vanillaDrawSeen; i++) {
			vanillaDrawSeen = !resolve(client, "music.creative", false).equals(SWEDEN);
		}
		Check.that(vanillaDrawSeen, "A non-music sound instance was curated");
	}

	private static void favoritesPlayMoreOften(Minecraft client, ModConfig cfg) {
		cfg.disabledSongs = new HashSet<>();
		cfg.favoriteSongs = Set.of(SWEDEN);
		RandomSource random = RandomSource.create(5);
		Map<String, Integer> counts = new HashMap<>();
		int draws = 40000;
		for (int i = 0; i < draws; i++) {
			counts.merge(draw(client, "music.game", random), 1, Integer::sum);
		}
		double favoriteCount = counts.getOrDefault(SWEDEN, 0);
		double otherAverage = (draws - favoriteCount) / (counts.size() - 1);
		// Weight 3 against 1, minus the no-immediate-repeat rule: about 2.7 times as often.
		double ratio = favoriteCount / otherAverage;
		Check.that(ratio > 2.3 && ratio < 3.2, "The favorite played %.2f times as often as the other tracks", ratio);
	}

	private static void timingFollowsThePlayerOnlyInWorlds(ModConfig cfg) throws ReflectiveOperationException {
		MusicController controller = MusicController.getInstance();
		Check.that(controller.withDelays(Musics.MENU) == Musics.MENU, "The title screen lost vanilla's timing");
		Check.that(controller.withDelays(Musics.END_BOSS) == Musics.END_BOSS, "The dragon fight lost vanilla's timing");
		Check.that(controller.withDelays(Musics.CREDITS) == Musics.CREDITS, "The credits lost vanilla's timing");

		cfg.maxDelaySeconds = 1800;
		Music game = controller.withDelays(Musics.GAME);
		Check.that(game.minDelay() == controller.minDelaySeconds() * 20 && game.maxDelay() == 1800 * 20,
				"In-world music got %d..%d ticks", game.minDelay(), game.maxDelay());

		// Vanilla's Music Frequency option: Constant would force 5 s, Default would cap the gap at 20 min.
		Method nextSongDelay = MusicManager.MusicFrequency.class.getDeclaredMethod("getNextSongDelay", Music.class, RandomSource.class);
		nextSongDelay.setAccessible(true);
		RandomSource random = RandomSource.create(6);
		int constant = (int) nextSongDelay.invoke(MusicManager.MusicFrequency.CONSTANT, game, random);
		Check.that(constant >= game.minDelay() && constant <= game.maxDelay(), "Constant frequency overrode the player's delay: %d", constant);
		boolean beyondTwentyMinutes = false;
		for (int i = 0; i < 200; i++) {
			beyondTwentyMinutes |= (int) nextSongDelay.invoke(MusicManager.MusicFrequency.DEFAULT, game, random) > 20 * 60 * 20;
		}
		Check.that(beyondTwentyMinutes, "Default frequency still caps the player's delay at 20 minutes");
		int menu = (int) nextSongDelay.invoke(MusicManager.MusicFrequency.CONSTANT, Musics.MENU, random);
		Check.that(menu == 100, "The title screen should keep vanilla's Constant gap of 100 ticks, got %d", menu);
	}

	private static String draw(Minecraft client, String eventName, RandomSource random) {
		return SongSelector.choose(event(client, eventName), random, SoundManager.EMPTY_SOUND).getLocation().getPath();
	}

	// The path MusicManager uses; music=false builds a jukebox instance (RECORDS category) instead.
	private static String resolve(Minecraft client, String eventName, boolean music) {
		SoundEvent event = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace(eventName));
		SoundInstance instance = music ? SimpleSoundInstance.forMusic(event) : SimpleSoundInstance.forJukeboxSong(event, Vec3.ZERO);
		instance.resolve(client.getSoundManager());
		return instance.getSound().getLocation().getPath();
	}

	private static WeighedSoundEvents event(Minecraft client, String name) {
		return client.getSoundManager().getSoundEvent(Identifier.withDefaultNamespace(name));
	}

	// The event's own files, without following references to other events.
	private static Set<String> ownFiles(Minecraft client, String eventName) {
		Set<String> files = new TreeSet<>();
		for (Weighted<Sound> entry : ((WeighedSoundEventsAccessor) (Object) event(client, eventName)).musiccurator$getList()) {
			if (entry instanceof Sound sound) {
				files.add(sound.getLocation().getPath());
			}
		}
		return files;
	}

	private static Set<String> allFilesExcept(String... kept) {
		Set<String> files = new HashSet<>();
		for (Track track : TrackRegistry.all()) {
			files.add(track.file());
		}
		files.removeAll(Set.of(kept));
		return files;
	}
}
