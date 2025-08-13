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
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.packet.PacketMineConfig;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class manages mine config files.
 */
public class MineConfigManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull Map<String, PacketMineConfig> packetMineConfigs = new HashMap<>();
    private final @NotNull Map<String, WorldMineConfig> worldMineConfigMap = new HashMap<>();

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     */
    public MineConfigManager(@NotNull SkyMines skyMines) {
        this.skyMines = skyMines;
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
        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines" + File.separator + "packet"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        PacketMineConfig mineConfig = null;
                        @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            mineConfig = loader.load().get(PacketMineConfig.class);
                        } catch (ConfigurateException e) {
                            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Failed to load packet mine config for " + path.toFile()));
                        }

                        if(mineConfig != null && mineConfig.mineId() != null) {
                            packetMineConfigs.put(mineConfig.mineId(), mineConfig);
                        } else {
                            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Failed to load packet mine config for " + path.toFile()));
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines" + File.separator + "world"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        WorldMineConfig mineConfig = null;
                        @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            mineConfig = loader.load().get(WorldMineConfig.class);
                        } catch (ConfigurateException e) {
                            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Failed to load world mine config for " + path.toFile()));
                        }

                        if(mineConfig != null && mineConfig.mineId() != null) {
                            worldMineConfigMap.put(mineConfig.mineId(), mineConfig);
                        } else {
                            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Failed to load world mine config for " + path.toFile()));
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
