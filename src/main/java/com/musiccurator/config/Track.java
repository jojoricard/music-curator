package com.musiccurator.config;

// One vanilla music track (a single .ogg) from tracks.json. "file" is the sound path without namespace
// or extension (e.g. "music/game/sweden"): it matches Sound#getLocation().getPath() and is the value
// stored in ModConfig.disabledSongs.
public record Track(String file, String title, String composer, String category) {
}
