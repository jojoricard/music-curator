package com.musiccurator.music;

import com.musiccurator.mixin.WeighedSoundEventsAccessor;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Picks the actual song to play for a music event, honouring the per-track selection.
// Strategy: gather the enabled songs in this event's pool and pick one (weighted),
// avoiding an immediate repeat. If the current biome offers none of the selected songs,
// fall back to any selected song seen elsewhere so the player's choice still plays.
public final class SongSelector {
	private static final Map<String, Sound> GLOBAL_ENABLED = new ConcurrentHashMap<>();
	@Nullable
	private static volatile String lastPicked = null;

	private SongSelector() {
	}

	public static Sound choose(WeighedSoundEvents event, RandomSource random, Sound chosenByVanilla) {
		MusicController controller = MusicController.getInstance();

		List<Sound> candidates = new ArrayList<>();
		collect(event, candidates, controller);

		Sound picked = pick(candidates, random);
		if (picked == null) {
			// No selected song belongs to this biome -> fall back to the wider selection.
			picked = pick(new ArrayList<>(GLOBAL_ENABLED.values()), random);
		}
		if (picked == null) {
			return chosenByVanilla; // nothing collected yet -> stay graceful
		}
		lastPicked = picked.getLocation().getPath();
		return picked;
	}

	private static void collect(Weighted<Sound> node, List<Sound> out, MusicController controller) {
		if (node instanceof Sound s) {
			Identifier loc = s.getLocation();
			if (loc != null && loc.getPath().startsWith("music/") && controller.isSongEnabled(loc)) {
				out.add(s);
				GLOBAL_ENABLED.put(loc.getPath(), s);
			}
		} else if (node instanceof WeighedSoundEvents nested) {
			for (Weighted<Sound> child : ((WeighedSoundEventsAccessor) (Object) nested).musiccurator$getList()) {
				collect(child, out, controller);
			}
		}
	}

	@Nullable
	private static Sound pick(List<Sound> candidates, RandomSource random) {
		if (candidates.isEmpty()) {
			return null;
		}
		// Prefer not repeating the last song (so skip always changes track when possible).
		List<Sound> pool = new ArrayList<>();
		String avoid = lastPicked;
		for (Sound s : candidates) {
			if (avoid == null || !s.getLocation().getPath().equals(avoid)) {
				pool.add(s);
			}
		}
		if (pool.isEmpty()) {
			pool = candidates;
		}
		int total = 0;
		for (Sound s : pool) {
			total += Math.max(1, s.getWeight());
		}
		int roll = random.nextInt(total);
		for (Sound s : pool) {
			roll -= Math.max(1, s.getWeight());
			if (roll < 0) {
				return s;
			}
		}
		return pool.get(pool.size() - 1);
	}
}
