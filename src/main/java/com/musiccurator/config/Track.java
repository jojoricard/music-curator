package com.musiccurator.config;

// One vanilla music entry. Loaded from tracks.json.
public record Track(String id, String name, String composer, String version, String category) {
}
