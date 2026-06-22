package com.musiccurator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.musiccurator.config.Preset;
import com.musiccurator.music.MusicController;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class MusicCuratorCommands {
	private MusicCuratorCommands() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("musiccurator")
				.then(ClientCommands.literal("skip").executes(ctx -> {
					MusicController.getInstance().skip();
					ctx.getSource().sendFeedback(Component.translatable("musiccurator.command.skip"));
					return 1;
				}))
				.then(ClientCommands.literal("hud")
						.then(ClientCommands.literal("toggle").executes(ctx -> {
							boolean shown = MusicController.getInstance().toggleHud();
							ctx.getSource().sendFeedback(Component.translatable(
									shown ? "musiccurator.command.hud_on" : "musiccurator.command.hud_off"));
							return 1;
						})))
				.then(ClientCommands.literal("preset")
						.then(ClientCommands.argument("name", StringArgumentType.word())
								.suggests(presetSuggestions())
								.executes(MusicCuratorCommands::applyPreset))));
	}

	private static int applyPreset(CommandContext<FabricClientCommandSource> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		Preset preset = Preset.byAlias(name);
		if (preset == null || preset == Preset.CUSTOM) {
			ctx.getSource().sendError(Component.translatable("musiccurator.command.preset_unknown", name));
			return 0;
		}
		MusicController.getInstance().applyPreset(preset);
		ctx.getSource().sendFeedback(Component.translatable("musiccurator.command.preset", name));
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
