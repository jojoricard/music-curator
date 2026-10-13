package com.musiccurator;

//? if fabric {
import com.musiccurator.command.MusicCuratorCommands;
import com.musiccurator.hud.MusicHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
// Fabric API renamed KeyBindingHelper to KeyMappingHelper when it adopted Mojang's names (26.1).
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}
// Fabric API has HudElementRegistry, which places the HUD below the chat, only since 1.21.6.
//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}

// Fabric entrypoint ("client" in fabric.mod.json).
public class MusicCuratorFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MusicCurator.init();

		for (KeyMapping key : MusicCuratorClient.KEYS) {
			//? if >=26.1 {
			KeyMappingHelper.registerKeyMapping(key);
			//?} else {
			/*KeyBindingHelper.registerKeyBinding(key);
			*///?}
		}

		//? if >=1.21.6 {
		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MusicCurator.MOD_ID, "music_hud"),
				MusicHudOverlay::render
		);
		//?} else {
		/*HudRenderCallback.EVENT.register(MusicHudOverlay::render);
		*///?}

		ClientTickEvents.END_CLIENT_TICK.register(MusicCuratorClient::onClientTick);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> MusicCuratorCommands.register(
				dispatcher, FabricClientCommandSource::sendFeedback, FabricClientCommandSource::sendError));
	}
}
//?}
