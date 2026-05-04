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
package com.github.lukesky19.skymines.gui;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.gui.config.WorldMinePreviewConfig;
import com.github.lukesky19.skymines.gui.config.WorldMineShopConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the loading of gui configuration files.
 */
public class GUIConfigManager {
    private final @NonNull SkyMines skyMines;
    private final @NonNull ComponentLogger logger;
    private @Nullable WorldMineShopConfig worldMineShopConfig;
    private @Nullable WorldMinePreviewConfig worldMinePreviewConfig;

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     */
    public GUIConfigManager(@NonNull SkyMines skyMines) {
        this.skyMines = skyMines;
        this.logger = skyMines.getComponentLogger();
    }

    /**
     * Get the {@link WorldMineShopConfig} for a world mine shop.
     * @return A {@link WorldMineShopConfig} or null.
     */
    public @Nullable WorldMineShopConfig getWorldMineShopConfig() {
        return worldMineShopConfig;
    }

    /**
     * Get the {@link WorldMinePreviewConfig} for a world mine preview GUI.
     * @return A {@link WorldMinePreviewConfig} or null.
     */
    public @Nullable WorldMinePreviewConfig getMinePreviewConfig() {
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

        YamlConfigurationLoader shopLoader = createLoader(shopPath);
        YamlConfigurationLoader previewLoader = createLoader(previewPath);

        try {
            ConfigurationNode shopRoot = shopLoader.load();

            migrateConfigVersion(shopRoot);

            WorldMineShopConfig worldMineShopConfig = shopRoot.get(WorldMineShopConfig.class);
            if(worldMineShopConfig != null) {
                WorldMineShopConfig migratedShopConfig = migrateWorldMineShopConfig(worldMineShopConfig);
                if(migratedShopConfig != null) {
                    if(migratedShopConfig != worldMineShopConfig) {
                        saveWorldMineShopConfig(migratedShopConfig);
                    }

                    this.worldMineShopConfig = migratedShopConfig;
                }
            }
        } catch (ConfigurateException e) {
            logger.warn(AdventureUtility.plain("Failed to load world mine shop config. Error: " + e.getMessage()));
        }

        try {
            ConfigurationNode previewRoot = previewLoader.load();

            migrateConfigVersion(previewRoot);

            WorldMinePreviewConfig worldMinePreviewConfig = previewRoot.get(WorldMinePreviewConfig.class);

            if(worldMinePreviewConfig != null) {
                WorldMinePreviewConfig migratedPreviewConfig = migrateWorldMinePreviewConfig(worldMinePreviewConfig);
                if(migratedPreviewConfig != null) {
                    if(migratedPreviewConfig != worldMinePreviewConfig) {
                        saveWorldMinePreviewConfig(migratedPreviewConfig);
                    }

                    this.worldMinePreviewConfig = migratedPreviewConfig;
                }
            }
        } catch (ConfigurateException e) {
            logger.warn(AdventureUtility.plain("Failed to load preview GUI config. Error: " + e.getMessage()));
        }
    }

    private @Nullable WorldMinePreviewConfig migrateWorldMinePreviewConfig(@NonNull WorldMinePreviewConfig worldMinePreviewConfig) {
        switch(worldMinePreviewConfig.version()) {
            case 2 -> {
                // Latest version, do nothing
                return worldMinePreviewConfig;
            }

            case 1 -> {
                return new WorldMinePreviewConfig(
                        2,
                        worldMinePreviewConfig.guiType(),
                        worldMinePreviewConfig.guiName(),
                        worldMinePreviewConfig.itemsPerPage(),
                        worldMinePreviewConfig.filler(),
                        worldMinePreviewConfig.nextPage(),
                        worldMinePreviewConfig.prevPage(),
                        worldMinePreviewConfig.exit(),
                        worldMinePreviewConfig.dummyButtons(),
                        worldMinePreviewConfig.slots());
            }

            default -> {
                logger.error(AdventureUtility.plain("Unable to migrate world mine preview config due to an unknown config version."));
                return null;
            }
        }
    }

    /**
     * Migrate the {@link WorldMineShopConfig} for the shop to the latest version.
     */
    private @Nullable WorldMineShopConfig migrateWorldMineShopConfig(@NonNull WorldMineShopConfig worldMineShopConfig) {
        switch(worldMineShopConfig.version()) {
            case 3 -> {
                // Latest version, do nothing
                return worldMineShopConfig;
            }

            case 2 -> {
                return new WorldMineShopConfig(
                        3,
                        worldMineShopConfig.guiType(),
                        worldMineShopConfig.guiName(),
                        worldMineShopConfig.itemsPerPage(),
                        worldMineShopConfig.filler(),
                        worldMineShopConfig.nextPage(),
                        worldMineShopConfig.prevPage(),
                        worldMineShopConfig.exit(),
                        worldMineShopConfig.currency(),
                        worldMineShopConfig.dummyButtons(),
                        worldMineShopConfig.slots());
            }

            case 1 -> {
                return new WorldMineShopConfig(
                        3,
                        worldMineShopConfig.guiType(),
                        worldMineShopConfig.guiName(),
                        worldMineShopConfig.itemsPerPage(),
                        worldMineShopConfig.filler(),
                        worldMineShopConfig.nextPage(),
                        worldMineShopConfig.prevPage(),
                        worldMineShopConfig.exit(),
                        new WorldMineShopConfig.ButtonConfig(
                                45,
                                new ItemStackConfig(
                                        ItemType.GOLD_INGOT,
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
            }

            default -> {
                logger.error(AdventureUtility.plain("Unable to migrate world mine shop config due to an unknown config version."));
                return null;
            }
        }
    }

    /**
     * Save the {@link WorldMineShopConfig} to the disk for the world mine shop.
     * The {@link WorldMineShopConfig} to save.
     */
    private void saveWorldMineShopConfig(@NonNull WorldMineShopConfig worldMineShopConfig) {
        try {
            Path shopPath = Path.of(skyMines.getDataFolder() + File.separator + "gui" + File.separator + "unlocks_shop.yml");

            YamlConfigurationLoader yamlConfigurationLoader = createLoader(shopPath);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(WorldMineShopConfig.class, worldMineShopConfig);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to save world mine gui config file. Error: " + e.getMessage()));
        }
    }

    /**
     * Save the {@link WorldMinePreviewConfig} to the disk for the world preview GUI.
     * The {@link WorldMinePreviewConfig} to save.
     */
    private void saveWorldMinePreviewConfig(@NonNull WorldMinePreviewConfig worldMinePreviewConfig) {
        try {
            Path previewPath = Path.of(skyMines.getDataFolder() + File.separator + "gui" + File.separator + "free_preview.yml");

            YamlConfigurationLoader yamlConfigurationLoader = createLoader(previewPath);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(WorldMinePreviewConfig.class, worldMinePreviewConfig);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to save world mine gui config file. Error: " + e.getMessage()));
        }
    }

    /**
     * Migrate the config version format.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migrateConfigVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        if(version == 0) {
            ConfigurationNode legacyVersionNode = root.node("config-version");
            String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
            try {
                switch (legacyVersion) {
                    case "1.1.0.0" -> versionNode.set(2);

                    case "1.0.0.0" -> versionNode.set(1);

                    case null, default -> logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
            }
        }
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @apiNote {@link PlatformUtils#getSerializers()} are included by default.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .defaultOptions(configurationOptions ->
                        configurationOptions.serializers(builder ->
                                builder.registerAll(PlatformUtils.getSerializers())))
                .build();
    }
}