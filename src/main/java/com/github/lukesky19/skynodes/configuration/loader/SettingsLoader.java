/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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
package com.github.lukesky19.skynodes.configuration.loader;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.record.Settings;
import com.github.lukesky19.skynodes.configuration.validator.SettingsValidator;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsLoader {
    final SkyNodes skyNodes;
    final SettingsValidator settingsValidator;
    final ConfigurationUtility configurationUtility;
    Settings settings;

    /**
     * Constructor
     * @param skyNodes The Plugin's Instance.
     * @param settingsValidator A SettingsValidator Instance.
     * @param configurationUtility A ConfigurationUtility Instance.
    */
    public SettingsLoader(
            SkyNodes skyNodes,
            SettingsValidator settingsValidator,
            ConfigurationUtility configurationUtility) {
        this.skyNodes = skyNodes;
        this.settingsValidator = settingsValidator;
        this.configurationUtility = configurationUtility;
    }

    /**
     * A getter to get the plugin's settings.
     * @return A SettingsConfiguration object that represents the plugin's settings.
    */
    public Settings getSettings() {
        return settings;
    }

    /**
     * A method to reload the plugin's settings config.
    */
    public void reload() {
        settings = null;

        Path path = Path.of(skyNodes.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyNodes.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = loader.load().get(Settings.class);
        } catch (ConfigurateException ignored) {}

        settingsValidator.validateSettings(settings);
    }
}
