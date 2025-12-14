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
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.world.WorldMineGUIConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the loading of gui configuration files.
 */
public class GUIConfigManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull ComponentLogger logger;
    private @Nullable WorldMineGUIConfig worldMineShopConfig;
    private @Nullable WorldMineGUIConfig worldMinePreviewConfig;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     */
    public GUIConfigManager(@NotNull SkyMines skyMines) {
        this.skyMines = skyMines;
        this.logger = skyMines.getComponentLogger();
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
            logger.error(AdventureUtil.deserialize("<red>Failed to load world mine shop config.</red> " + e.getMessage()));
        }

        try {
            worldMinePreviewConfig = previewLoader.load().get(WorldMineGUIConfig.class);

            migrateWorldMineShopConfig();
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load preview GUI config.</red> " + e.getMessage()));
        }
    }

    /**
     * Migrate the {@link WorldMineGUIConfig} for the shop to the latest version.
     */
    private void migrateWorldMineShopConfig() {
        if(worldMineShopConfig == null) return;

        switch(worldMineShopConfig.configVersion()) {
            case "1.1.0.0" -> {
                // Latest version, do nothing
            }

            case "1.0.0.0" -> {
                worldMineShopConfig = new WorldMineGUIConfig(
                        "1.1.0.0",
                        worldMineShopConfig.guiType(),
                        worldMineShopConfig.guiName(),
                        worldMineShopConfig.itemsPerPage(),
                        worldMineShopConfig.filler(),
                        worldMineShopConfig.nextPage(),
                        worldMineShopConfig.prevPage(),
                        worldMineShopConfig.exit(),
                        new WorldMineGUIConfig.ButtonConfig(
                                45,
                                new ItemStackConfig(
                                        ItemType.GOLD_INGOT.getKey().toString(),
                                        1,
                                        null,
                                        "<white>Click to change currencies.</white>",
                                        List.of("<gray>Selected Currency:</gray> <white><currency></white>"),
                                        null,
                                        null,
                                        List.of(),
                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                        null,
                                        List.of(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        List.of(),
                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null))),
                        worldMineShopConfig.dummyButtons(),
                        worldMineShopConfig.slots());

                saveWorldMineShopConfig(worldMineShopConfig);
            }

            case null, default -> logger.error(AdventureUtil.deserialize("Unable to migrate world mine shop config due to an unknown config version."));
        }
    }

    /**
     * Save the {@link WorldMineGUIConfig} to the disk for the world mine shop.
     * The {@link WorldMineGUIConfig} to save.
     */
    private void saveWorldMineShopConfig(@NotNull WorldMineGUIConfig worldMineGUIConfig) {
        try {
            Path shopPath = Path.of(skyMines.getDataFolder() + File.separator + "gui" + File.separator + "unlocks_shop.yml");

            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(shopPath);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(WorldMineGUIConfig.class, worldMineGUIConfig);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.deserialize("Failed to save world mine gui config file. Error: " + e.getMessage()));
        }
    }
}
