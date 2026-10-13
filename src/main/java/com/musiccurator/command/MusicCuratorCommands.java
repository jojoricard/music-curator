package com.musiccurator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Preset;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.music.MusicController;
import com.musiccurator.screen.ConfigScreenBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

// Client-side "/musiccurator" commands. skip(), togglePause() and toggleHud() are shared with the keybinds,
// which show the returned message in the action bar instead of the chat.
// Each loader has its own client command source type (S) and way of printing to the chat, which its
// entrypoint passes to register().
public final class MusicCuratorCommands<S> {
	private final BiConsumer<S, Component> feedback;
	private final BiConsumer<S, Component> error;

	private MusicCuratorCommands(BiConsumer<S, Component> feedback, BiConsumer<S, Component> error) {
		this.feedback = feedback;
		this.error = error;
	}

	public static <S> void register(CommandDispatcher<S> dispatcher, BiConsumer<S, Component> feedback,
			BiConsumer<S, Component> error) {
		new MusicCuratorCommands<>(feedback, error).registerOn(dispatcher);
	}

	private void registerOn(CommandDispatcher<S> dispatcher) {
		dispatcher.register(literal("musiccurator")
				.then(literal("skip").executes(ctx -> reply(ctx, skip())))
				.then(literal("pause").executes(ctx -> reply(ctx, togglePause())))
				.then(literal("status").executes(this::status))
				.then(literal("config").executes(this::openConfig))
				.then(literal("hud")
						.then(literal("toggle").executes(ctx -> reply(ctx, toggleHud()))))
				.then(literal("preset")
						.then(argument("name", StringArgumentType.word())
								.suggests(presetSuggestions())
								.executes(this::applyPreset))));
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

	private LiteralArgumentBuilder<S> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	private <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}

	private int reply(CommandContext<S> ctx, Component message) {
		feedback.accept(ctx.getSource(), message);
		return 1;
	}

	private int openConfig(CommandContext<S> ctx) {
		Minecraft client = Minecraft.getInstance();
		// The chat screen closes itself right after running a command, which would close a screen opened now.
		//? if >1.21.1 {
		client.schedule(() -> ConfigScreenBuilder.open(client));
		//?} else {
		/*client.tell(() -> ConfigScreenBuilder.open(client));
		*///?}
		return 1;
	}

	private int status(CommandContext<S> ctx) {
		ModConfig cfg = ConfigManager.get();
		if (!cfg.enabled) {
			return reply(ctx, Component.translatable("musiccurator.status.disabled"));
		}
		MusicController controller = MusicController.getInstance();
		long enabledTracks = TrackRegistry.all().stream().filter(t -> !cfg.disabledSongs.contains(t.file())).count();
		reply(ctx, Component.translatable("musiccurator.status.selection",
				Component.translatable(cfg.preset.translationKey()), enabledTracks, TrackRegistry.all().size(),
				cfg.favoriteSongs.size()));
		reply(ctx, nowPlaying(controller));
		int min = controller.minDelaySeconds();
		int max = controller.maxDelaySeconds();
		return reply(ctx, min == max
				? Component.translatable("musiccurator.status.delay_fixed", min)
				: Component.translatable("musiccurator.status.delay", min, max));
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

	private int applyPreset(CommandContext<S> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		Preset preset = Preset.byAlias(name);
		// CUSTOM is a label for hand-picked selections, not something that can be applied.
		if (preset == null || preset == Preset.CUSTOM) {
			error.accept(ctx.getSource(), Component.translatable("musiccurator.command.preset_unknown", name));
			return 0;
		}
		MusicController.getInstance().applyPreset(preset);
		return reply(ctx, Component.translatable("musiccurator.command.preset",
				Component.translatable(preset.translationKey())));
	}

	private SuggestionProvider<S> presetSuggestions() {
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
