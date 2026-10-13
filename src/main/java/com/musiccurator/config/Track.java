package com.musiccurator.config;

import org.jetbrains.annotations.Nullable;

// One vanilla music track (a single .ogg) from tracks.json. "file" is the sound path without namespace
// or extension (e.g. "music/game/sweden"): it matches Sound#getLocation().getPath() and is the value
// stored in ModConfig.disabledSongs. "since" is the first Minecraft version that has the track, for
// tracks newer than the oldest version the mod supports.
public record Track(String file, String title, String composer, String category, @Nullable String since) {
}
