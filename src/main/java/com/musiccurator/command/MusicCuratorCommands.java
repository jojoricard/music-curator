package com.musiccurator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.music.MusicController;
import com.musiccurator.screen.ConfigScreenBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// Client-side "/musiccurator" commands. skip(), togglePause() and toggleHud() are shared with the keybinds,
// which show the returned message in the action bar instead of the chat.
public final class MusicCuratorCommands {
	private MusicCuratorCommands() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("musiccurator")
				.then(ClientCommands.literal("skip").executes(ctx -> reply(ctx, skip())))
				.then(ClientCommands.literal("pause").executes(ctx -> reply(ctx, togglePause())))
				.then(ClientCommands.literal("status").executes(MusicCuratorCommands::status))
				.then(ClientCommands.literal("config").executes(MusicCuratorCommands::openConfig))
				.then(ClientCommands.literal("hud")
						.then(ClientCommands.literal("toggle").executes(ctx -> reply(ctx, toggleHud()))))
				.then(ClientCommands.literal("preset")
						.then(ClientCommands.argument("name", StringArgumentType.word())
								.suggests(presetSuggestions())
								.executes(MusicCuratorCommands::applyPreset))));
	}

	public static Component skip() {
		MusicController.getInstance().skip();
		return Component.translatable("musiccurator.command.skip");
	}

	public static Component togglePause() {
		MusicController controller = MusicController.getInstance();
		if (!controller.isEnabled()) {
			return Component.translatable("musiccurator.command.disabled");
		}
		return Component.translatable(controller.togglePause() ? "musiccurator.command.paused" : "musiccurator.command.resumed");
	}

	public static Component toggleHud() {
		boolean shown = MusicController.getInstance().toggleHud();
		return Component.translatable(shown ? "musiccurator.command.hud_on" : "musiccurator.command.hud_off");
	}

	private static int reply(CommandContext<FabricClientCommandSource> ctx, Component message) {
		ctx.getSource().sendFeedback(message);
		return 1;
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		Minecraft client = ctx.getSource().getClient();
		// The chat screen closes itself right after running a command, which would close a screen opened now.
		client.schedule(() -> client.gui.setScreen(ConfigScreenBuilder.build(null)));
		return 1;
	}

	private static int status(CommandContext<FabricClientCommandSource> ctx) {
		FabricClientCommandSource source = ctx.getSource();
		ModConfig cfg = ConfigManager.get();
		if (!cfg.enabled) {
			source.sendFeedback(Component.translatable("musiccurator.status.disabled"));
			return 1;
		}
		MusicController controller = MusicController.getInstance();
		long enabledTracks = TrackRegistry.all().stream().filter(t -> !cfg.disabledSongs.contains(t.file())).count();
		source.sendFeedback(Component.translatable("musiccurator.status.selection",
				Component.translatable(cfg.preset.translationKey()), enabledTracks, TrackRegistry.all().size(),
				cfg.favoriteSongs.size()));
		source.sendFeedback(nowPlaying(controller));
		int min = controller.minDelaySeconds();
		int max = controller.maxDelaySeconds();
		source.sendFeedback(min == max
				? Component.translatable("musiccurator.status.delay_fixed", min)
				: Component.translatable("musiccurator.status.delay", min, max));
		return 1;
	}

	private static Component nowPlaying(MusicController controller) {
		Identifier song = controller.currentSong();
		if (song == null) {
			return Component.translatable(controller.isPaused() ? "musiccurator.hud.paused" : "musiccurator.status.idle");
		}
		Track track = TrackRegistry.get(song.getPath());
		// Same "Composer - Title" form as the game's own "Now Playing" toast.
		String name = track != null ? track.composer() + " - " + track.title() : TrackRegistry.titleOf(song.getPath());
		return Component.translatable(controller.isPaused() ? "musiccurator.status.paused" : "musiccurator.status.playing", name);
	}

	private static int applyPreset(CommandContext<FabricClientCommandSource> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		Preset preset = Preset.byAlias(name);
		// CUSTOM is a label for hand-picked selections, not something that can be applied.
		if (preset == null || preset == Preset.CUSTOM) {
			ctx.getSource().sendError(Component.translatable("musiccurator.command.preset_unknown", name));
			return 0;
		}
		MusicController.getInstance().applyPreset(preset);
		ctx.getSource().sendFeedback(Component.translatable("musiccurator.command.preset",
				Component.translatable(preset.translationKey())));
		return 1;
	}

	private static SuggestionProvider<FabricClientCommandSource> presetSuggestions() {
		return (ctx, builder) -> {
			for (Preset p : Preset.values()) {
				if (p != Preset.CUSTOM) {
					builder.suggest(p.alias());
				}
			}
			return builder.buildFuture();
		};
	}
}
