package com.musiccurator.music;

import com.musiccurator.mixin.SoundEventReferenceAccessor;
import com.musiccurator.mixin.WeighedSoundEventsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Picks the track a music event plays, honouring the per-track selection:
//  1. a weighted pick among the event's enabled tracks (favorites weigh more), avoiding an immediate repeat;
//  2. if the event has tracks but none is enabled, the same pick among the enabled tracks of every music
//     event, so the selection stays audible in contexts whose own pool is fully disabled;
//  3. if no track is enabled anywhere, silence.
// Only called from the client thread, where MusicManager starts songs.
public final class SongSelector {
	// Vanilla names every situational music event "music.<context>"; jukebox discs are "music_disc.<name>".
	private static final String MUSIC_EVENT_PREFIX = "music.";
	// The lang files describe favorites as playing "three times as often": update them together.
	private static final int FAVORITE_WEIGHT = 3;

	@Nullable
	private static String lastPicked = null;

	private SongSelector() {
	}

	public static Sound choose(WeighedSoundEvents event, RandomSource random, Sound vanillaPick) {
		List<Sound> tracks = new ArrayList<>();
		collect(event, tracks);
		if (tracks.isEmpty()) {
			// Events without any track (e.g. music.nether.warped_forest) stay silent, as in vanilla.
			return vanillaPick;
		}

		Sound picked = pick(enabledOnly(tracks), random);
		if (picked == null) {
			picked = pick(enabledOnly(allMusicTracks()), random);
		}
		if (picked == null) {
			// Vanilla's "play nothing" marker: SoundEngine skips it without logging a warning.
			return SoundManager.INTENTIONALLY_EMPTY_SOUND;
		}
		lastPicked = picked.getLocation().getPath();
		return picked;
	}

	// Flattens an event into its .ogg entries. Each file keeps its own weight, which reproduces vanilla's
	// odds since a referenced event weighs the sum of its entries. A null entry (reference to a missing
	// event) contributes nothing.
	private static void collect(@Nullable Weighted<Sound> entry, List<Sound> out) {
		if (entry instanceof Sound track) {
			// Vanilla never draws a weight-0 entry.
			if (track.getWeight() > 0) {
				out.add(track);
			}
		} else if (entry instanceof WeighedSoundEvents event) {
			for (Weighted<Sound> child : ((WeighedSoundEventsAccessor) (Object) event).musiccurator$getList()) {
				collect(child, out);
			}
		} else if (entry instanceof SoundEventReferenceAccessor reference) {
			collect(Minecraft.getInstance().getSoundManager().getSoundEvent(reference.musiccurator$getEntry().getLocation()), out);
		}
	}

	// One entry per file: a track listed in several biomes must not weigh more than the others.
	private static Collection<Sound> allMusicTracks() {
		SoundManager soundManager = Minecraft.getInstance().getSoundManager();
		Map<String, Sound> byFile = new HashMap<>();
		for (Identifier id : soundManager.getAvailableSounds()) {
			if (!id.getPath().startsWith(MUSIC_EVENT_PREFIX)) {
				continue;
			}
			List<Sound> tracks = new ArrayList<>();
			collect(soundManager.getSoundEvent(id), tracks);
			for (Sound track : tracks) {
				byFile.putIfAbsent(track.getLocation().getPath(), track);
			}
		}
		return byFile.values();
	}

	private static List<Sound> enabledOnly(Collection<Sound> tracks) {
		MusicController controller = MusicController.getInstance();
		return tracks.stream().filter(track -> controller.isSongEnabled(track.getLocation())).toList();
	}

	@Nullable
	private static Sound pick(List<Sound> candidates, RandomSource random) {
		if (candidates.isEmpty()) {
			return null;
		}
		// Leave out the previous track when there is an alternative, so "skip" always changes song.
		List<Sound> pool = candidates.stream()
				.filter(track -> !track.getLocation().getPath().equals(lastPicked))
				.toList();
		if (pool.isEmpty()) {
			pool = candidates;
		}
		int total = 0;
		for (Sound track : pool) {
			total += weightOf(track);
		}
		int roll = random.nextInt(total);
		for (Sound track : pool) {
			roll -= weightOf(track);
			if (roll < 0) {
				return track;
			}
		}
		throw new AssertionError("A roll below the total weight always lands on a track");
	}

	private static int weightOf(Sound track) {
		boolean favorite = MusicController.getInstance().isFavorite(track.getLocation());
		return favorite ? track.getWeight() * FAVORITE_WEIGHT : track.getWeight();
	}
}
