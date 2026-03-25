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
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.Settings;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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
    public SettingsManager(@NonNull SkyMines skyMines) {
        super(skyMines, Path.of(skyMines.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    /**
     * Load the settings configuration.
     */
    public void loadConfiguration() {
        configuration = null;

        if(configurationPath == null) return;

        saveBundledConfig();

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();

            migrateVersion(root);

            Settings settings = root.get(Settings.class);
            if(settings == null) {
                logger.warn(AdventureUtil.deserialize("Failed to load configuration file settings.yml. Class name: " + this.getClass().getName()));
                return;
            }

            // Migrate configuration
            Settings migratedConfiguration = migrateConfiguration(settings);
            if(migratedConfiguration == null) return;

            if(migratedConfiguration != settings) {
                saveConfiguration(migratedConfiguration);
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(migratedConfiguration)) {
                logger.warn(AdventureUtil.deserialize("Settings configuration validation failed. Class name: " + this.getClass().getName()));
                return;
            }

            this.configuration = migratedConfiguration;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load configuration. Error: " + configurateException.getMessage()));
        }
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
    public @Nullable Settings migrateConfiguration(@NonNull Settings configuration) {
        switch(configuration.version()) {
            case 5 -> {
                // Latest version, do nothing.
                return configuration;
            }

            case 4 -> {
                return new Settings(
                        5,
                        configuration.locale(),
                        configuration.messageCooldownDurationSeconds());
            }

            case 3 -> {
                return new Settings(
                        5,
                        configuration.locale(),
                        10);
            }

            default -> {
                logger.warn(AdventureUtil.deserialize("Unable to migrate settings because the config version is not recognized."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration(@Nullable Settings settings) {
        return settings != null && settings.locale() != null;
    }

    /**
     * Migrate the config version format.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migrateVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        if(version == 0) {
            ConfigurationNode legacyVersionNode = root.node("config-version");
            @Nullable String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
            try {
                switch (legacyVersion) {
                    case "3.1.0.0" -> versionNode.set(4);

                    case "3.0.0.0" -> versionNode.set(3);

                    case null, default -> logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtil.deserialize("Failed to convert String-based version to numeric version"));
            }
        }
    }
}