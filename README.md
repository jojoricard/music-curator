# Music Curator

Client mod for Minecraft 26.2. Pick which vanilla music can play, mark favorites, set how often music plays, pause it without losing your place, and see the current track in a small HUD. Available in English and French.

| The HUD | `/musiccurator status` |
| --- | --- |
| ![HUD showing the current track, its composer and category](docs/screenshots/hud.png) | ![Status command output in the chat](docs/screenshots/status.png) |
| **Tracks tab** | **Favorites tab** |
| ![Settings screen, Tracks tab with the C418 group expanded](docs/screenshots/tracks.png) | ![Settings screen, Favorites tab](docs/screenshots/favorites.png) |

The screenshots are produced by the client game tests (`ScreenshotTest`); to refresh them, run `./gradlew runClientGameTest` and copy `build/run/clientGameTest/screenshots/000N_music-curator-<name>.png` to `docs/screenshots/<name>.png`.

## Requirements


| Dependency    | Required                    | Tested version |
| ------------- | --------------------------- | -------------- |
| Minecraft     | yes                         | 26.2           |
| Fabric Loader | yes                         | 0.19.3         |
| Fabric API    | yes                         | 0.152.2+26.2   |
| Cloth Config  | yes (settings screen)       | 26.2.155       |
| Mod Menu      | no (adds the config button) | 20.0.0-beta.3  |


## Install

Drop the jar into your `mods/` folder, along with Fabric API and Cloth Config. Mod Menu is optional: the settings screen also opens with `/musiccurator config` or the *Open settings* keybind.

## Settings

Open with `/musiccurator config`, the *Open settings* keybind, or Mod Menu → Music Curator → Config. Sections:

- **General** — master toggle, preset, min/max delay between tracks, fixed interval
- **HUD** — show/hide, anchor, x/y offset, composer/category line
- **Tracks** — every vanilla track, grouped by composer, each with its own toggle
- **Favorites** — tracks that play three times as often as the others

Picking a preset replaces the whole track selection. Toggling individual tracks switches the preset to *Custom*, unless the result matches a preset. Disabling the track that is playing stops it right away.

## Presets


| Preset                | Alias    | Plays                                  |
| --------------------- | -------- | -------------------------------------- |
| All vanilla           | `all`    | every track                            |
| C418 only             | `c418`   | tracks composed by C418                |
| Modern only (no C418) | `modern` | every track not composed by C418       |
| Custom                | —        | your own selection from the Tracks tab |


A preset is a rule, not a snapshot: when an update adds tracks or corrects a composer, your preset follows on the next launch. Presets never change your favorites.

## Commands and keybinds

Client-side commands:

- `/musiccurator skip` — play another track now
- `/musiccurator pause` — pause or resume the music
- `/musiccurator status` — preset, enabled tracks, favorites, current track and delays
- `/musiccurator config` — open the settings screen
- `/musiccurator hud toggle` — show or hide the HUD
- `/musiccurator preset <all|c418|modern>` — apply a preset

Keybinds for skip, pause/resume, HUD toggle and open settings are unbound by default — set them in Options → Controls → Key Binds → Music Curator. Their result shows above the hotbar.

## Notes

- Only background music is affected. Jukebox discs and all other sounds play as usual.
- When the current biome or situation has none of your tracks, a track from your selection plays instead. With every track disabled, no music plays.
- A new track never repeats the previous one when another is available, so skipping always changes the song.
- The delays apply to background music in worlds, where they replace vanilla's Options → Music & Sounds → Music Frequency. The title screen, the Ender Dragon fight and the end credits keep the game's timing.
- Pausing freezes the current track and resuming continues it where it stopped; skipping while paused plays the next track. The pause lasts until you resume or restart the game, and the HUD shows it.

## Config file

Settings are saved to `config/music-curator.json`. `disabledSongs` lists the sound files that must not play, such as `music/game/sweden`, and `favoriteSongs` the ones that play more often. When you edit `disabledSongs` by hand, also set `"preset": "CUSTOM"`, otherwise the preset's rule replaces your list on the next launch.

If the file cannot be parsed, it is renamed to `music-curator.json.broken` and defaults are loaded, so you can fix it and rename it back.

## Build from source

Needs JDK 25.

```
./gradlew build              # jar lands in build/libs/
./gradlew runClient          # launch a dev client
./gradlew runClientGameTest  # run the client game tests (opens the game, needs an audio device)
```
