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
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.packet.PacketMineConfig;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.database.DatabaseManager;
import com.github.lukesky19.skymines.database.tables.MineIdsTable;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class manages mine config files.
 */
public class MineConfigManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull ComponentLogger logger;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull Map<String, PacketMineConfig> packetMineConfigs = new HashMap<>();
    private final @NotNull Map<String, WorldMineConfig> worldMineConfigMap = new HashMap<>();

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public MineConfigManager(@NotNull SkyMines skyMines, @NotNull DatabaseManager databaseManager) {
        this.skyMines = skyMines;
        this.logger = skyMines.getComponentLogger();
        this.databaseManager = databaseManager;
    }

    /**
     * Get the {@link WorldMineConfig} for the mine id provided.
     * @param mineId The mine id to get the config for.
     * @return A {@link WorldMineConfig} or null.
     */
    public @Nullable WorldMineConfig getWorldMineConfig(@NotNull String mineId) {
        return worldMineConfigMap.get(mineId);
    }

    /**
     * Get a {@link Map} mapping mine ids to {@link PacketMineConfig}s.
     * @return A {@link Map} mapping mine ids to {@link PacketMineConfig}s.
     */
    public @NotNull Map<String, PacketMineConfig> getPacketMineConfigs() {
        return packetMineConfigs;
    }

    /**
     * Get a {@link Map} mapping mine ids to {@link WorldMineConfig}s.
     * @return A {@link Map} mapping mine ids to {@link WorldMineConfig}s.
     */
    public @NotNull Map<String, WorldMineConfig> getWorldMineConfigs() {
        return worldMineConfigMap;
    }

    /**
     * Loads all mine config files in the mines folder.
     */
    public void reload() {
        @NotNull MineIdsTable mineIdsTable = databaseManager.getMineIdsTable();
        packetMineConfigs.clear();
        worldMineConfigMap.clear();

        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines" + File.separator + "packet"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        PacketMineConfig mineConfig = null;
                        @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            mineConfig = loader.load().get(PacketMineConfig.class);
                        } catch (ConfigurateException e) {
                            logger.warn(AdventureUtil.deserialize("Failed to load packet mine config for " + path.toFile()));
                        }

                        if(mineConfig != null && mineConfig.mineId() != null) {
                            mineIdsTable.insertMineId(mineConfig.mineId());

                            packetMineConfigs.put(mineConfig.mineId(), mineConfig);
                        } else {
                            logger.warn(AdventureUtil.deserialize("Failed to load packet mine config for " + path.toFile()));
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines" + File.separator + "world"))) {
            for(Path path : paths.filter(Files::isRegularFile).toList()) {
                WorldMineConfig mineConfig;
                @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                String fileName = path.toFile().getName();
                try {
                    mineConfig = loader.load().get(WorldMineConfig.class);
                } catch (ConfigurateException e) {
                    logger.warn(AdventureUtil.deserialize("Failed to load world mine config for " + fileName));
                    continue;
                }

                if(mineConfig != null) {
                    @Nullable WorldMineConfig migratedMineConfig = migrateWorldMineConfig(mineConfig, fileName);
                    if(migratedMineConfig == null) {
                        logger.warn(AdventureUtil.deserialize("Failed to migrate world mine config for " + fileName));
                        continue;
                    }

                    if(mineConfig != migratedMineConfig) {
                        saveWorldMineConfig(path, migratedMineConfig);
                    }

                    if(migratedMineConfig.mineId() != null) {
                        mineIdsTable.insertMineId(migratedMineConfig.mineId());

                        worldMineConfigMap.put(migratedMineConfig.mineId(), migratedMineConfig);
                    } else {
                        logger.warn(AdventureUtil.deserialize("The world mine config for " + fileName + " has an invalid mine id."));
                    }
                } else {
                    logger.warn(AdventureUtil.deserialize("Failed to load world mine config for " + fileName));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Migrate the {@link WorldMineConfig} to the latest version.
     * @param worldMineConfig The {@link WorldMineConfig} to migrate.
     * @return The migrated {@link WorldMineConfig} or null.
     */
    private @Nullable WorldMineConfig migrateWorldMineConfig(@NotNull WorldMineConfig worldMineConfig, @NotNull String fileName) {
        switch(worldMineConfig.configVersion()) {
            case "1.1.0.0" -> {
                // Latest version, do nothing
                return worldMineConfig;
            }

            case "1.0.0.0" -> {
                List<WorldMineConfig.UnlockBlockData> migratedUnlockBlockData = worldMineConfig.unlockableBreakable().stream().map(unlockBlockData -> {
                    return new WorldMineConfig.UnlockBlockData(
                            unlockBlockData.blockType(),
                            unlockBlockData.displayItemLocked(),
                            unlockBlockData.displayItemUnlocked(),
                            new WorldMineConfig.PriceData(unlockBlockData.buyPrice(), -1),
                            null);
                }).toList();

                return new WorldMineConfig(
                        "1.1.0.0",
                        worldMineConfig.mineId(),
                        worldMineConfig.worldName(),
                        worldMineConfig.canPlacePlayerBlocks(),
                        worldMineConfig.canBreakPlayerBlocks(),
                        worldMineConfig.restrictPlaceToUnlockedAndFree(),
                        worldMineConfig.allowPlayerExplosions(),
                        worldMineConfig.bossBar(),
                        migratedUnlockBlockData,
                        worldMineConfig.freeBreakable(),
                        worldMineConfig.restrictedPlaceable());
            }

            case null, default -> {
                logger.warn(AdventureUtil.deserialize("Unknown config version for world mine config " + fileName));
                return null;
            }
        }
    }

    /**
     * Save the {@link WorldMineConfig} to the disk.
     * The {@link WorldMineConfig} to save.
     */
    private void saveWorldMineConfig(@NotNull Path path, @NotNull WorldMineConfig worldMineConfig) {
        try {
            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(WorldMineConfig.class, worldMineConfig);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.deserialize("Failed to save world mine config file. Error: " + e.getMessage()));
        }
    }
}
