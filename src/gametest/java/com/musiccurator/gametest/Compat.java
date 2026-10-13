package com.musiccurator.gametest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.jetbrains.annotations.Nullable;

// The vanilla calls the game tests need that differ between the supported Minecraft versions.
final class Compat {
	private Compat() {
	}

	// Screens moved from Minecraft to its Gui in 26.1.
	@Nullable
	static Screen screen(Minecraft client) {
		//? if >=26.1 {
		return client.gui.screen();
		//?} else {
		/*return client.screen;
		*///?}
	}

	// Applies a changed GUI scale; renamed from resizeDisplay() in 26.1.
	static void resizeGui(Minecraft client) {
		//? if >=26.1 {
		client.resizeGui();
		//?} else {
		/*client.resizeDisplay();
		*///?}
	}

	// Draws the sound file the instance will play; renamed from resolve() in 26.3.
	static void resolve(SoundInstance instance, Minecraft client) {
		//? if >=26.3 {
		/*instance.getOrResolve(client.getSoundManager());
		*///?} else {
		instance.resolve(client.getSoundManager());
		//?}
	}
}
