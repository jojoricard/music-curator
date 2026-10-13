package com.musiccurator.hud;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.music.MusicController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

// 26.1 renamed GuiGraphics to GuiGraphicsExtractor, and drawString to text.
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

// HUD box in a screen corner showing the playing track (title, then composer and/or category), or
// that music is paused.
public final class MusicHudOverlay {
	private static final int PADDING = 4;
	private static final int LINE_SPACING = 1;
	private static final int BG_COLOR = 0xB0101014;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int SUB_COLOR = 0xFFB0B0B0;

	private MusicHudOverlay() {
	}

	//? if >=26.1 {
	public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
	//?} else {
	/*public static void render(GuiGraphics graphics, DeltaTracker delta) {
	*///?}
		ModConfig cfg = ConfigManager.get();
		if (!cfg.enabled || !cfg.showHud) {
			return;
		}
		List<Component> lines = lines(cfg);
		if (lines.isEmpty()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;

		int boxWidth = 0;
		for (Component line : lines) {
			boxWidth = Math.max(boxWidth, font.width(line));
		}
		boxWidth += PADDING * 2;
		int boxHeight = PADDING * 2 + lines.size() * font.lineHeight + (lines.size() - 1) * LINE_SPACING;

		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();

		int x = switch (cfg.hudAnchor) {
			case TOP_LEFT, BOTTOM_LEFT -> cfg.hudOffsetX;
			case TOP_RIGHT, BOTTOM_RIGHT -> screenW - boxWidth - cfg.hudOffsetX;
		};
		int y = switch (cfg.hudAnchor) {
			case TOP_LEFT, TOP_RIGHT -> cfg.hudOffsetY;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> screenH - boxHeight - cfg.hudOffsetY;
		};

		graphics.fill(x, y, x + boxWidth, y + boxHeight, BG_COLOR);

		int textY = y + PADDING;
		for (int i = 0; i < lines.size(); i++) {
			int color = i == 0 ? TITLE_COLOR : SUB_COLOR;
			//? if >=26.1 {
			graphics.text(font, lines.get(i), x + PADDING, textY, color, true);
			//?} else {
			/*graphics.drawString(font, lines.get(i), x + PADDING, textY, color, true);
			*///?}
			textY += font.lineHeight + LINE_SPACING;
		}
	}

	private static List<Component> lines(ModConfig cfg) {
		MusicController controller = MusicController.getInstance();
		Identifier song = controller.currentSong();
		if (song == null) {
			// Paused between two songs: there is no track to show, only the reason nothing plays.
			return controller.isPaused() ? List.of(Component.translatable("musiccurator.hud.paused")) : List.of();
		}

		String title = TrackRegistry.titleOf(song.getPath());
		List<Component> lines = new ArrayList<>();
		lines.add(controller.isPaused()
				? Component.translatable("musiccurator.hud.paused_track", title)
				: Component.literal(title));
		Track track = TrackRegistry.get(song.getPath());
		if (track != null && (cfg.hudShowComposer || cfg.hudShowCategory)) {
			lines.add(subtitle(cfg, track));
		}
		return lines;
	}

	private static Component subtitle(ModConfig cfg, Track track) {
		MutableComponent subtitle = Component.empty();
		if (cfg.hudShowComposer) {
			subtitle.append(track.composer());
		}
		if (cfg.hudShowCategory) {
			if (cfg.hudShowComposer) {
				subtitle.append(" \u00b7 ");
			}
			subtitle.append(Component.translatable("musiccurator.category." + track.category()));
		}
		return subtitle;
	}
}
