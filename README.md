# Music Curator

Client mod for Minecraft 26.2. Pick which vanilla music can play, set how often it plays, and see the current track in a small HUD.

## Requirements


| Dependency    | Required                    | Tested version |
| ------------- | --------------------------- | -------------- |
| Minecraft     | yes                         | 26.2           |
| Fabric Loader | yes                         | 0.19.3         |
| Fabric API    | yes                         | 0.152.2+26.2   |
| Cloth Config  | yes (settings screen)       | 26.2.155       |
| Mod Menu      | no (adds the config button) | 20.0.0-beta.3  |


## Install

Drop the jar into your `mods/` folder, along with Fabric API and Cloth Config. Add Mod Menu if you want a config button in the mods list.

## Settings

Open with Mod Menu → Music Curator → Config. Sections:

- **General** — master toggle, preset, min/max delay, override vanilla delay
- **HUD** — show/hide, anchor, x/y offset, composer/category lines
- **Tracks** — every vanilla track, grouped by category, with checkboxes

Picking a preset overwrites the track selection. Toggling individual tracks switches the preset to *Custom*.

## Presets

Each version preset is cumulative — it keeps everything from the steps before it.


| Preset                   | Tracks                                   |
| ------------------------ | ---------------------------------------- |
| All vanilla              | Every ambient track                      |
| C418 only                | Tracks composed by C418 (plus his discs) |
| + Nether Update (1.16)   | C418 + 1.16                              |
| + Caves & Cliffs (1.18)  | previous + 1.18                          |
| + The Wild Update (1.19) | previous + 1.19                          |
| + 1.20 / 1.21 / 26.x     | previous + everything newer              |


## Commands

Client-side only:

- `/musiccurator skip`
- `/musiccurator hud toggle`
- `/musiccurator preset <all|c418|nether|caves|wild|latest>`

Keybinds for skip, pause/resume, and HUD toggle are unbound by default — set them in Options → Controls.

## Notes

Only ambient (situational) music goes through the game's `MusicManager`, so that is what the mod filters. Jukebox discs are listed for the presets but are not affected in-game.

## Build from source

Needs JDK 25.

```
./gradlew build      # jar lands in build/libs/
./gradlew runClient  # launch a dev client
```

