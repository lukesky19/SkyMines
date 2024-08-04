/*
    SkyNodes places a random configured schematic after a set period of time.
    Copyright (C) 2023  lukeskywlker19

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
package com.github.lukesky19.skynodes.configuration.settings;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.utils.ConfigurationUtility;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages everything related to handling the plugin's settings.
*/
public class SettingsManager {
    final SkyNodes skyNodes;
    final ConfigurationUtility configurationUtility;
    SettingsConfiguration settingsConfiguration;

    /**
     * Constructor
     * @param skyNodes The plugin's instance.
     * @param configurationUtility A ConfigurationUtility instance.
    */
    public SettingsManager(SkyNodes skyNodes, ConfigurationUtility configurationUtility) {
        this.skyNodes = skyNodes;
        this.configurationUtility = configurationUtility;
    }

    /**
     * A getter to get the plugin's settings.
     * @return A SettingsConfiguration object that represents the plugin's settings.
    */
    public SettingsConfiguration getSettings() {
        return settingsConfiguration;
    }

    /**
     * A method to reload the plugin's settings config.
    */
    public void reload() {
        settingsConfiguration = null;

        skyNodes.saveResource("settings.yml", false);

        Path path = Path.of(skyNodes.getDataFolder() + File.separator + "settings.yml");
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            settingsConfiguration = loader.load().get(SettingsConfiguration.class);
        } catch (ConfigurateException exception) {
            skyNodes.setPluginState(false);
        }
    }
}
