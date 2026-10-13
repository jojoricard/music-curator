package com.musiccurator.gametest;

import com.musiccurator.mixin.MusicManagerAccessor;
import com.musiccurator.mixin.SoundEngineAccessor;
import com.musiccurator.mixin.SoundManagerAccessor;
import com.musiccurator.music.MusicController;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

// Needs a working audio device: game tests mute the music volume, but songs still get a real channel.
public class PlaybackTest implements FabricClientGameTest {
	private static final int ONE_MINUTE = 20 * 60;

	@Override
	public void runTest(ClientGameTestContext context) {
		pauseFreezesTheSongInsteadOfStoppingIt(context);
		commandsWorkFromTheChat(context);
	}

	// Uses the title screen music.
	private static void pauseFreezesTheSongInsteadOfStoppingIt(ClientGameTestContext context) {
		context.waitFor(client -> currentMusic(client) != null && client.getSoundManager().isActive(currentMusic(client)), ONE_MINUTE);
		SoundInstance song = context.computeOnClient(PlaybackTest::currentMusic);
		waitForChannel(context, song, true);

		context.runOnClient(client -> MusicController.getInstance().togglePause());
		waitForChannel(context, song, false);
		Check.that(context.computeOnClient(client -> currentMusic(client) == song && client.getSoundManager().isActive(song)),
				"Pausing stopped the song instead of freezing it");

		// Vanilla calls this whenever a screen closes; it unpauses every channel.
		context.runOnClient(client -> client.getSoundManager().resume());
		waitForChannel(context, song, false);

		context.runOnClient(client -> MusicController.getInstance().togglePause());
		waitForChannel(context, song, true);
		Check.that(context.computeOnClient(client -> currentMusic(client) == song),
				"Resuming started another song instead of continuing the paused one");
	}

	private static void commandsWorkFromTheChat(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			// The chat screen closes right after running a command: the settings screen must outlive that.
			runInChat(context, "/musiccurator config");
			Component title = Component.translatable("musiccurator.config.title");
			context.waitFor(client -> Compat.screen(client) != null && Compat.screen(client).getTitle().equals(title));
			context.setScreen(() -> null);

			boolean pausedBefore = context.computeOnClient(client -> MusicController.getInstance().isPaused());
			runInChat(context, "/musiccurator pause");
			Check.that(context.computeOnClient(client -> MusicController.getInstance().isPaused()) != pausedBefore,
					"/musiccurator pause did not toggle the pause");
			runInChat(context, "/musiccurator pause");
			Check.that(context.computeOnClient(client -> MusicController.getInstance().isPaused()) == pausedBefore,
					"/musiccurator pause did not toggle the pause back");
		}
	}

	private static void runInChat(ClientGameTestContext context, String command) {
		context.getInput().pressKey(options -> options.keyChat);
		context.waitForScreen(ChatScreen.class);
		context.getInput().typeChars(command);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitTick();
	}

	@Nullable
	private static SoundInstance currentMusic(Minecraft client) {
		return ((MusicManagerAccessor) client.getMusicManager()).musiccurator$getCurrentMusic();
	}

	// Channel state lives on the sound thread: ask it, then wait for the answer, until it matches.
	private static void waitForChannel(ClientGameTestContext context, SoundInstance song, boolean playing) {
		for (int attempt = 0; attempt < 100; attempt++) {
			AtomicReference<Boolean> answer = new AtomicReference<>();
			context.runOnClient(client -> {
				SoundEngine engine = ((SoundManagerAccessor) client.getSoundManager()).musiccurator$getSoundEngine();
				ChannelAccess.ChannelHandle channel = ((SoundEngineAccessor) engine).musiccurator$getInstanceToChannel().get(song);
				Check.that(channel != null, "The song has no audio channel any more");
				channel.execute(c -> answer.set(c.playing()));
			});
			context.waitFor(client -> answer.get() != null);
			if (answer.get() == playing) {
				return;
			}
			context.waitTick();
		}
		throw new AssertionError("The song's channel never became " + (playing ? "playing" : "paused"));
	}
}
