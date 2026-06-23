package com.musiccurator.config;

// One vanilla music track (a single .ogg), loaded from tracks.json.
// "file" is the sound file path as seen in-game, e.g. "music/game/sweden".
public record Track(String file, String title, String composer, String category) {
}
