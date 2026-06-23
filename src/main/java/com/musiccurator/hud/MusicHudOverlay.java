package com.musiccurator.hud;

import com.musiccurator.config.ConfigManager;
import com.musiccurator.config.ModConfig;
import com.musiccurator.config.Track;
import com.musiccurator.config.TrackRegistry;
import com.musiccurator.music.MusicController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class MusicHudOverlay {
	private static final int PADDING = 4;
	private static final int BG_COLOR = 0xB0101014;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int SUB_COLOR = 0xFFB0B0B0;

	private MusicHudOverlay() {
	}

	public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		ModConfig cfg = ConfigManager.get();
		if (!cfg.enabled || !cfg.showHud) {
			return;
		}

		Identifier current = MusicController.getInstance().currentSong();
		if (current == null) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;

		Track track = TrackRegistry.get(current.getPath());
		String name = track != null ? track.title() : prettify(current.getPath());
		if (MusicController.getInstance().isPaused()) {
			name = name + " (" + Component.translatable("musiccurator.hud.paused").getString() + ")";
		}

		List<Component> lines = new ArrayList<>();
		lines.add(Component.literal(name));

		if (track != null) {
			String sub = subtitle(cfg, track);
			if (!sub.isEmpty()) {
				lines.add(Component.literal(sub));
			}
		}

		int boxWidth = 0;
		for (Component line : lines) {
			boxWidth = Math.max(boxWidth, font.width(line));
		}
		boxWidth += PADDING * 2;
		int boxHeight = PADDING * 2 + lines.size() * font.lineHeight + (lines.size() - 1);

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
			graphics.text(font, lines.get(i), x + PADDING, textY, color, true);
			textY += font.lineHeight + 1;
		}
	}

	// Fallback for songs missing from the registry: "music/game/foo_bar" -> "Foo Bar".
	private static String prettify(String path) {
		String base = path.substring(path.lastIndexOf('/') + 1).replace('_', ' ');
		StringBuilder sb = new StringBuilder(base.length());
		boolean cap = true;
		for (char c : base.toCharArray()) {
			sb.append(cap ? Character.toUpperCase(c) : c);
			cap = c == ' ';
		}
		return sb.toString();
	}

	private static String subtitle(ModConfig cfg, Track track) {
		StringBuilder sb = new StringBuilder();
		if (cfg.hudShowComposer && track.composer() != null && !track.composer().isEmpty()) {
			sb.append(track.composer());
		}
		if (cfg.hudShowCategory && track.category() != null && !track.category().isEmpty()) {
			if (sb.length() > 0) {
				sb.append(" \u00b7 ");
			}
			sb.append(track.category());
		}
		return sb.toString();
	}
}
