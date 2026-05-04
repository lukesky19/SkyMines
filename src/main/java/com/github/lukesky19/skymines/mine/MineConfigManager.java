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
package com.github.lukesky19.skymines.mine;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.database.DatabaseManager;
import com.github.lukesky19.skymines.database.tables.MineIdsTable;
import com.github.lukesky19.skymines.mine.config.PacketMineConfig;
import com.github.lukesky19.skymines.mine.config.WorldMineConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    private final @NonNull SkyMines skyMines;
    private final @NonNull ComponentLogger logger;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull Map<String, PacketMineConfig> packetMineConfigs = new HashMap<>();
    private final @NonNull Map<String, WorldMineConfig> worldMineConfigMap = new HashMap<>();

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public MineConfigManager(@NonNull SkyMines skyMines, @NonNull DatabaseManager databaseManager) {
        this.skyMines = skyMines;
        this.logger = skyMines.getComponentLogger();
        this.databaseManager = databaseManager;
    }

    /**
     * Get the {@link WorldMineConfig} for the mine id provided.
     * @param mineId The mine id to get the config for.
     * @return A {@link WorldMineConfig} or null.
     */
    public @Nullable WorldMineConfig getWorldMineConfig(@NonNull String mineId) {
        return worldMineConfigMap.get(mineId);
    }

    /**
     * Get a {@link Map} mapping mine ids to {@link PacketMineConfig}s.
     * @return A {@link Map} mapping mine ids to {@link PacketMineConfig}s.
     */
    public @NonNull Map<String, PacketMineConfig> getPacketMineConfigs() {
        return packetMineConfigs;
    }

    /**
     * Get a {@link Map} mapping mine ids to {@link WorldMineConfig}s.
     * @return A {@link Map} mapping mine ids to {@link WorldMineConfig}s.
     */
    public @NonNull Map<String, WorldMineConfig> getWorldMineConfigs() {
        return worldMineConfigMap;
    }

    /**
     * Loads all mine config files in the mines folder.
     */
    public void reload() {
        MineIdsTable mineIdsTable = databaseManager.getMineIdsTable();
        packetMineConfigs.clear();
        worldMineConfigMap.clear();

        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines" + File.separator + "packet"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        PacketMineConfig mineConfig = null;
                        YamlConfigurationLoader loader = createLoader(path);
                        String fileName = path.toFile().getName();

                        try {
                            ConfigurationNode root = loader.load();

                            migratePacketMineConfigVersion(root);

                            mineConfig = root.get(PacketMineConfig.class);
                        } catch (ConfigurateException e) {
                            logger.warn(AdventureUtility.plain("Failed to load packet mine config for " + path.toFile()));
                        }

                        if(mineConfig != null) {
                            PacketMineConfig migratedMineConfig = migratePacketMineConfig(mineConfig, fileName);
                            if(migratedMineConfig != null) {
                                if(mineConfig != migratedMineConfig) {
                                    savePacketMineConfig(path, migratedMineConfig);
                                }

                                if(migratedMineConfig.mineId() != null) {
                                    mineIdsTable.insertMineId(migratedMineConfig.mineId());

                                    packetMineConfigs.put(migratedMineConfig.mineId(), migratedMineConfig);
                                } else {
                                    logger.warn(AdventureUtility.plain("Failed to load packet mine config for " + path.toFile()));
                                }
                            } else {
                                logger.warn(AdventureUtility.plain("Failed to migrate packet mine config for " + fileName));
                            }
                        } else {
                            logger.warn(AdventureUtility.plain("Failed to load packet mine config for " + fileName));
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines" + File.separator + "world"))) {
            for(Path path : paths.filter(Files::isRegularFile).toList()) {
                WorldMineConfig mineConfig;
                YamlConfigurationLoader loader = createLoader(path);
                String fileName = path.toFile().getName();

                try {
                    ConfigurationNode root = loader.load();

                    migrateWorldMineConfigVersion(root);

                    mineConfig = root.get(WorldMineConfig.class);
                } catch (ConfigurateException e) {
                    logger.warn(AdventureUtility.plain("Failed to load world mine config for " + fileName));
                    continue;
                }

                if(mineConfig != null) {
                    WorldMineConfig migratedMineConfig = migrateWorldMineConfig(mineConfig, fileName);
                    if(migratedMineConfig == null) {
                        logger.warn(AdventureUtility.plain("Failed to migrate world mine config for " + fileName));
                        continue;
                    }

                    if(mineConfig != migratedMineConfig) {
                        saveWorldMineConfig(path, migratedMineConfig);
                    }

                    if(migratedMineConfig.mineId() != null) {
                        mineIdsTable.insertMineId(migratedMineConfig.mineId());

                        worldMineConfigMap.put(migratedMineConfig.mineId(), migratedMineConfig);
                    } else {
                        logger.warn(AdventureUtility.plain("The world mine config for " + fileName + " has an invalid mine id."));
                    }
                } else {
                    logger.warn(AdventureUtility.plain("Failed to load world mine config for " + fileName));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Migrate the {@link PacketMineConfig} to the latest version.
     * @param packetMineConfig The {@link PacketMineConfig} to migrate.
     * @return The migrated {@link PacketMineConfig} or null.
     */
    private @Nullable PacketMineConfig migratePacketMineConfig(@NonNull PacketMineConfig packetMineConfig, @NonNull String fileName) {
        switch(packetMineConfig.version()) {
            case 4 -> {
                // Latest version, do nothing
                return packetMineConfig;
            }

            case 3 -> {
                return new PacketMineConfig(
                        4,
                        packetMineConfig.mineId(),
                        packetMineConfig.bossBar(),
                        packetMineConfig.worldName(),
                        packetMineConfig.parentRegion(),
                        packetMineConfig.childRegions());
            }

            default -> {
                logger.warn(AdventureUtility.plain("Unable to migrate packet mine config due to an unknown config version for " + packetMineConfig.version() + " in file " + fileName));
                return null;
            }
        }
    }

    /**
     * Migrate the {@link WorldMineConfig} to the latest version.
     * @param worldMineConfig The {@link WorldMineConfig} to migrate.
     * @return The migrated {@link WorldMineConfig} or null.
     */
    private @Nullable WorldMineConfig migrateWorldMineConfig(@NonNull WorldMineConfig worldMineConfig, @NonNull String fileName) {
        switch(worldMineConfig.version()) {
            case 3 -> {
                // Latest version, do nothing
                return worldMineConfig;
            }

            case 2 -> {
                return new WorldMineConfig(
                        3,
                        worldMineConfig.mineId(),
                        worldMineConfig.worldName(),
                        worldMineConfig.canPlacePlayerBlocks(),
                        worldMineConfig.canBreakPlayerBlocks(),
                        worldMineConfig.restrictPlaceToUnlockedAndFree(),
                        worldMineConfig.allowPlayerExplosions(),
                        true,
                        true,
                        false,
                        false,
                        true,
                        true,
                        true,
                        true,
                        true,
                        worldMineConfig.bossBar(),
                        worldMineConfig.unlockableBreakable(),
                        worldMineConfig.freeBreakable(),
                        worldMineConfig.restrictedPlaceable());
            }

            case 1 -> {
                List<WorldMineConfig.UnlockBlockData> migratedUnlockBlockData = worldMineConfig.unlockableBreakable().stream().map(unlockBlockData -> {
                    return new WorldMineConfig.UnlockBlockData(
                            unlockBlockData.blockType(),
                            unlockBlockData.displayItemLocked(),
                            unlockBlockData.displayItemUnlocked(),
                            new WorldMineConfig.PriceData(unlockBlockData.buyPrice(), -1),
                            null);
                }).toList();

                return new WorldMineConfig(
                        3,
                        worldMineConfig.mineId(),
                        worldMineConfig.worldName(),
                        worldMineConfig.canPlacePlayerBlocks(),
                        worldMineConfig.canBreakPlayerBlocks(),
                        worldMineConfig.restrictPlaceToUnlockedAndFree(),
                        worldMineConfig.allowPlayerExplosions(),
                        true,
                        true,
                        false,
                        false,
                        true,
                        true,
                        true,
                        true,
                        true,
                        worldMineConfig.bossBar(),
                        migratedUnlockBlockData,
                        worldMineConfig.freeBreakable(),
                        worldMineConfig.restrictedPlaceable());
            }

            default -> {
                logger.warn(AdventureUtility.plain("Unable to migrate world mine config due to an unknown config version for " + worldMineConfig.version() + " in file " + fileName));
                return null;
            }
        }
    }

    /**
     * Save the {@link PacketMineConfig} to the disk.
     * The {@link PacketMineConfig} to save.
     */
    private void savePacketMineConfig(@NonNull Path path, @NonNull PacketMineConfig packetMineConfig) {
        try {
            YamlConfigurationLoader loader = createLoader(path);

            ConfigurationNode node = loader.createNode();

            node.set(PacketMineConfig.class, packetMineConfig);

            loader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to save packet mine config file. Error: " + e.getMessage()));
        }
    }

    /**
     * Save the {@link WorldMineConfig} to the disk.
     * The {@link WorldMineConfig} to save.
     */
    private void saveWorldMineConfig(@NonNull Path path, @NonNull WorldMineConfig worldMineConfig) {
        try {
            YamlConfigurationLoader loader = createLoader(path);

            ConfigurationNode node = loader.createNode();

            node.set(WorldMineConfig.class, worldMineConfig);

            loader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to save world mine config file. Error: " + e.getMessage()));
        }
    }

    /**
     * Migrate the config version format.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migratePacketMineConfigVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        if(version == 0) {
            ConfigurationNode legacyVersionNode = root.node("config-version");
            String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
            try {
                switch (legacyVersion) {
                    case "3.0.0.0" -> versionNode.set(3);

                    case null, default -> logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
            }
        }
    }

    /**
     * Migrate the config version format.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migrateWorldMineConfigVersion(@NonNull ConfigurationNode root) {
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