/*
    SkyMines tracks blocks broken in specific regions, replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2025  lukeskywlker19

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
package com.github.lukesky19.skymines.configuration.loader;

import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.record.Settings;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager {
    private final SkyMines skyMines;
    private Settings settings;

    /**
     * Constructor
     * @param skyMines The Plugin's Instance.
    */
    public SettingsManager(
            SkyMines skyMines) {
        this.skyMines = skyMines;
    }

    /**
     * A getter to get the plugin's settings.
     * @return A SettingsConfiguration object that represents the plugin's settings.
    */
    @Nullable
    public Settings getSettings() {
        return settings;
    }

    /**
     * A method to reload the plugin's settings config.
    */
    public void reload() {
        ComponentLogger logger = skyMines.getComponentLogger();
        settings = null;

        Path path = Path.of(skyMines.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyMines.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = loader.load().get(Settings.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (ConfigurateException configurateException) {
            logger.error(FormatUtil.format("<red>Failed to load plugin settings.</red>"));
            if(configurateException.getMessage() != null) {
                logger.error(FormatUtil.format(configurateException.getMessage()));
            }
        }
    }
}
