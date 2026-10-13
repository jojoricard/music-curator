package com.musiccurator;

//? if neoforge {
/*import com.musiccurator.command.MusicCuratorCommands;
import com.musiccurator.hud.MusicHudOverlay;
import com.musiccurator.screen.ConfigScreenBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

// NeoForge entrypoint. Client-only: NeoForge does not construct it on a dedicated server.
@Mod(value = MusicCurator.MOD_ID, dist = Dist.CLIENT)
public class MusicCuratorNeoForge {
	public MusicCuratorNeoForge(IEventBus modBus, ModContainer container) {
		MusicCurator.init();

		modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
			//? if >=1.21.9 {
			event.registerCategory(MusicCuratorClient.CATEGORY);
			//?}
			MusicCuratorClient.KEYS.forEach(event::register);
		});

		modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerBelow(
				VanillaGuiLayers.CHAT,
				Identifier.fromNamespaceAndPath(MusicCurator.MOD_ID, "music_hud"),
				MusicHudOverlay::render
		));

		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,
				event -> MusicCuratorClient.onClientTick(Minecraft.getInstance()));

		NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> MusicCuratorCommands.register(
				event.getDispatcher(), (source, message) -> source.sendSuccess(() -> message, false),
				CommandSourceStack::sendFailure));

		// The "Config" button of NeoForge's mod list.
		container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> ConfigScreenBuilder.build(parent));
	}
}
*///?}
