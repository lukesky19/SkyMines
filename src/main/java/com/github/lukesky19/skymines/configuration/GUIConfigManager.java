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
package com.github.lukesky19.skymines.configuration;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.world.WorldMineGUIConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the loading of gui configuration files.
 */
public class GUIConfigManager {
    private final @NotNull SkyMines skyMines;
    private @Nullable WorldMineGUIConfig worldMineShopConfig;
    private @Nullable WorldMineGUIConfig worldMinePreviewConfig;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     */
    public GUIConfigManager(@NotNull SkyMines skyMines) {
        this.skyMines = skyMines;
    }

    /**
     * Get the {@link WorldMineGUIConfig} for a world mine shop.
     * @return A {@link WorldMineGUIConfig} or null.
     */
    public @Nullable WorldMineGUIConfig getWorldMineShopConfig() {
        return worldMineShopConfig;
    }

    /**
     * Get the {@link WorldMineGUIConfig} for a world mine preview GUI.
     * @return A {@link WorldMineGUIConfig} or null.
     */
    public @Nullable WorldMineGUIConfig getMinePreviewConfig() {
        return worldMinePreviewConfig;
    }

    /**
     * A method to reload the plugin's gui config.
     */
    public void reload() {
        ComponentLogger logger = skyMines.getComponentLogger();
        worldMineShopConfig = null;
        worldMinePreviewConfig = null;

        Path shopPath = Path.of(skyMines.getDataFolder() + File.separator + "gui" + File.separator + "unlocks_shop.yml");
        Path previewPath = Path.of(skyMines.getDataFolder() + File.separator + "gui" + File.separator + "free_preview.yml");
        if(!shopPath.toFile().exists()) {
            skyMines.saveResource("gui/unlocks_shop.yml", false);
        }
        if(!previewPath.toFile().exists()) {
            skyMines.saveResource("gui/free_preview.yml", false);
        }

        YamlConfigurationLoader shopLoader = ConfigurationUtility.getYamlConfigurationLoader(shopPath);
        YamlConfigurationLoader previewLoader = ConfigurationUtility.getYamlConfigurationLoader(previewPath);

        try {
            worldMineShopConfig = shopLoader.load().get(WorldMineGUIConfig.class);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("<red>Failed to load world mine shop config.</red>"));
            if(e.getMessage() != null) {
                logger.error(AdventureUtil.serialize(e.getMessage()));
            }
        }

        try {
            worldMinePreviewConfig = previewLoader.load().get(WorldMineGUIConfig.class);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.serialize("<red>Failed to load preview GUI config.</red>"));
            if(e.getMessage() != null) {
                logger.error(AdventureUtil.serialize(e.getMessage()));
            }
        }
    }
}
