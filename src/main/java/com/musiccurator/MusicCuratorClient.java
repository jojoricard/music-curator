package com.musiccurator;

import com.mojang.blaze3d.platform.InputConstants;
import com.musiccurator.command.MusicCuratorCommands;
import com.musiccurator.hud.MusicHudOverlay;
import com.musiccurator.music.MusicController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class MusicCuratorClient implements ClientModInitializer {
	private static KeyMapping skipKey;
	private static KeyMapping pauseKey;
	private static KeyMapping hudKey;

	@Override
	public void onInitializeClient() {
		registerKeybinds();

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MusicCurator.MOD_ID, "music_hud"),
				MusicHudOverlay::render
		);

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, access) -> MusicCuratorCommands.register(dispatcher)
		);
	}

	private void registerKeybinds() {
		// Unbound by default to avoid clashing with other mods; bind in Controls.
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(MusicCurator.MOD_ID, "general"));
		skipKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.musiccurator.skip", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
		pauseKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.musiccurator.pause", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
		hudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.musiccurator.toggle_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
	}

	private void onClientTick(Minecraft client) {
		MusicController controller = MusicController.getInstance();
		controller.clientTick();

		while (skipKey.consumeClick()) {
			controller.skip();
		}
		while (pauseKey.consumeClick()) {
			controller.togglePause();
		}
		while (hudKey.consumeClick()) {
			controller.toggleHud();
		}
	}
}
