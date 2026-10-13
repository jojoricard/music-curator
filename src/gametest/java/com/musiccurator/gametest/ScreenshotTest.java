package com.musiccurator.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.music.MusicController;
import com.musiccurator.screen.ConfigScreenBuilder;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.ClothConfigTabButton;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

// Renders the HUD, /musiccurator status and the settings tabs in a real world and saves the screenshots
// shown in the README (versions/<version>/build/run/clientGameTest/screenshots; copy them to docs/screenshots).
public class ScreenshotTest implements FabricClientGameTest {
	private static final int HALF_A_MINUTE = 20 * 30;

	@Override
	public void runTest(ClientGameTestContext context) {
		ModConfig cfg = context.computeOnClient(client -> ConfigManager.get());
		boolean showCategory = cfg.hudShowCategory;
		Set<String> favorites = cfg.favoriteSongs;
		context.getInput().resizeWindow(1920, 1080);
		// The automatic GUI scale picks 4 at 1080p, which makes everything look zoomed in; players
		// at that resolution usually play at 2.
		int guiScale = context.computeOnClient(client -> client.options.guiScale().get());
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			Compat.resizeGui(client);
		});
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			context.runOnClient(client -> {
				cfg.hudShowCategory = true;
				cfg.favoriteSongs = Set.of("music/game/sweden", "music/game/wet_hands", "music/game/creative/aria_math");
				MusicController.getInstance().skip();
			});
			context.waitFor(client -> MusicController.getInstance().currentSong() != null, HALF_A_MINUTE);
			// Keep the cursor away from widgets so no tooltip covers the shots.
			context.getInput().setCursorPos(0, 0);
			context.waitTicks(10);
			context.takeScreenshot("music-curator-hud");

			context.getInput().pressKey(options -> options.keyChat);
			context.waitForScreen(ChatScreen.class);
			context.getInput().typeChars("/musiccurator status");
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(5);
			context.takeScreenshot("music-curator-status");

			context.setScreen(() -> ConfigScreenBuilder.build(null));
			openTab(context, "musiccurator.config.category.tracks");
			expandFirstComposer(context);
			context.takeScreenshot("music-curator-tracks");

			openTab(context, "musiccurator.config.category.favorites");
			expandFirstComposer(context);
			context.takeScreenshot("music-curator-favorites");
			context.setScreen(() -> null);
		} finally {
			context.runOnClient(client -> {
				cfg.hudShowCategory = showCategory;
				cfg.favoriteSongs = favorites;
				client.options.guiScale().set(guiScale);
				Compat.resizeGui(client);
			});
		}
	}

	// Cloth keeps its tab buttons in a private list rather than among the screen's widgets, so the
	// game test API cannot click them by name.
	private static void openTab(ClientGameTestContext context, String categoryKey) {
		try {
			context.runOnClient(client -> {
				Field field = ClothConfigScreen.class.getDeclaredField("tabButtons");
				field.setAccessible(true);
				for (Object tab : (List<?>) field.get(Compat.screen(client))) {
					ClothConfigTabButton button = (ClothConfigTabButton) tab;
					if (button.getMessage().equals(Component.translatable(categoryKey))) {
						button.onPress(null);
						return;
					}
				}
				throw new AssertionError("No settings tab " + categoryKey);
			});
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Cloth Config no longer keeps its tabs in ClothConfigScreen#tabButtons", e);
		}
		context.waitTicks(2);
	}

	private static void expandFirstComposer(ClientGameTestContext context) {
		context.runOnClient(client -> {
			ClothConfigScreen screen = (ClothConfigScreen) Compat.screen(client);
			for (Object entry : screen.listWidget.children()) {
				if (entry instanceof SubCategoryListEntry group) {
					group.setExpanded(true);
					return;
				}
			}
			throw new AssertionError("No composer group on the selected tab");
		});
		context.getInput().setCursorPos(0, 0);
		context.waitTicks(5);
	}
}
