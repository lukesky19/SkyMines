/*
    SkyMines offers different types mines to get resources from.
    Copyright (C) 2023 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skymines.manager.config;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager extends SimpleConfigManager<Settings> {
    /**
     * Constructor
     * @param skyMines The Plugin's Instance.
    */
    public SettingsManager(@NotNull SkyMines skyMines) {
        super(skyMines, Path.of(skyMines.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    protected void saveBundledConfig() {
        if(configurationPath == null) return;

        if(!configurationPath.toFile().exists()) {
            plugin.saveResource("settings.yml", false);
        }
    }

    /**
     * Migrate the configuration to the latest version.
     * @param configuration The configuration to migrate.
     * @return The migrated configuration or null.
     */
    @Override
    protected @Nullable Settings migrateConfiguration(@NotNull Settings configuration) {
        switch(configuration.configVersion()) {
            case "3.1.0.0" -> {
                // Latest version, do nothing.
                return configuration;
            }

            case "3.0.0.0" -> {
                return new Settings(
                        "3.1.0.0",
                        configuration.locale(),
                        10);
            }

            case null -> {
                logger.warn(AdventureUtil.deserialize("Unable to migrate settings because the config version is not configured."));
                return null;
            }

            default -> {
                logger.warn(AdventureUtil.deserialize("Unable to migrate settings because the config version is not recognized."));
                return null;
            }
        }
    }

    @Override
    protected boolean validateConfiguration() {
        return true;
    }
}
