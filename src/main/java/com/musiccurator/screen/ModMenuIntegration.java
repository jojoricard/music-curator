package com.musiccurator.screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// Only loaded when Mod Menu is present (it reads the "modmenu" entrypoint), so referencing
// Mod Menu classes here is safe even though it is an optional dependency.
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreenBuilder::build;
	}
}
