package com.musiccurator;

import com.mojang.blaze3d.platform.InputConstants;
import com.musiccurator.command.MusicCuratorCommands;
import com.musiccurator.hud.MusicHudOverlay;
import com.musiccurator.music.MusicController;
import com.musiccurator.screen.ConfigScreenBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

// Client wiring: keybinds, the HUD element, the per-tick controller update and the client commands.
public class MusicCuratorClient implements ClientModInitializer {
	private static KeyMapping skipKey;
	private static KeyMapping pauseKey;
	private static KeyMapping hudKey;
	private static KeyMapping configKey;

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
		skipKey = registerKey("key.musiccurator.skip", category);
		pauseKey = registerKey("key.musiccurator.pause", category);
		hudKey = registerKey("key.musiccurator.toggle_hud", category);
		configKey = registerKey("key.musiccurator.open_config", category);
	}

	private static KeyMapping registerKey(String name, KeyMapping.Category category) {
		return KeyMappingHelper.registerKeyMapping(
				new KeyMapping(name, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
	}

	// Keybinds only fire while no screen is open, so the action bar is always visible to the player.
	private void onClientTick(Minecraft client) {
		MusicController.getInstance().clientTick();

		while (skipKey.consumeClick()) {
			showInActionBar(client, MusicCuratorCommands.skip());
		}
		while (pauseKey.consumeClick()) {
			showInActionBar(client, MusicCuratorCommands.togglePause());
		}
		while (hudKey.consumeClick()) {
			showInActionBar(client, MusicCuratorCommands.toggleHud());
		}
		while (configKey.consumeClick()) {
			client.gui.setScreen(ConfigScreenBuilder.build(null));
		}
	}

	private static void showInActionBar(Minecraft client, Component message) {
		client.gui.hud.setOverlayMessage(message, false);
	}
}
