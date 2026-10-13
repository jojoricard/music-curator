package com.musiccurator;

import com.mojang.blaze3d.platform.InputConstants;
import com.musiccurator.command.MusicCuratorCommands;
import com.musiccurator.music.MusicController;
import com.musiccurator.screen.ConfigScreenBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
//? if >=1.21.9 {
import net.minecraft.resources.Identifier;
//?}

import java.util.List;

// Keybinds and per-tick work shared by both loaders; each loader's entrypoint registers them its own way.
public final class MusicCuratorClient {
	//? if >=1.21.9 && fabric {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MusicCurator.MOD_ID, "general"));
	//?} else if >=1.21.9 {
	/*// NeoForge wants it registered through RegisterKeyMappingsEvent instead (see MusicCuratorNeoForge).
	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
			Identifier.fromNamespaceAndPath(MusicCurator.MOD_ID, "general"));
	*///?} else {
	/*// The translation key that 1.21.9+ derives from the category identifier above.
	private static final String CATEGORY = "key.category.musiccurator.general";
	*///?}

	private static final KeyMapping SKIP_KEY = key("key.musiccurator.skip");
	private static final KeyMapping PAUSE_KEY = key("key.musiccurator.pause");
	private static final KeyMapping HUD_KEY = key("key.musiccurator.toggle_hud");
	private static final KeyMapping CONFIG_KEY = key("key.musiccurator.open_config");
	public static final List<KeyMapping> KEYS = List.of(SKIP_KEY, PAUSE_KEY, HUD_KEY, CONFIG_KEY);

	private MusicCuratorClient() {
	}

	// Unbound by default to avoid clashing with other mods; bind in Controls.
	private static KeyMapping key(String name) {
		return new KeyMapping(name, InputConstants.UNKNOWN.getValue(), CATEGORY);
	}

	// Keybinds only fire while no screen is open, so the action bar is always visible to the player.
	public static void onClientTick(Minecraft client) {
		MusicController.getInstance().clientTick();

		while (SKIP_KEY.consumeClick()) {
			showInActionBar(client, MusicCuratorCommands.skip());
		}
		while (PAUSE_KEY.consumeClick()) {
			showInActionBar(client, MusicCuratorCommands.togglePause());
		}
		while (HUD_KEY.consumeClick()) {
			showInActionBar(client, MusicCuratorCommands.toggleHud());
		}
		while (CONFIG_KEY.consumeClick()) {
			ConfigScreenBuilder.open(client);
		}
	}

	private static void showInActionBar(Minecraft client, Component message) {
		//? if >=26.1 {
		client.gui.hud.setOverlayMessage(message, false);
		//?} else {
		/*client.gui.setOverlayMessage(message, false);
		*///?}
	}
}
