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
package com.github.lukesky19.skymines.configuration;

import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.config.MineConfig;
import com.github.lukesky19.skymines.manager.MineManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * This class manages the loading of mine config files.
 */
public class MineConfigManager {
    private final SkyMines skyMines;
    private final MineManager mineManager;

    /**
     * Constructor
     * @param skyMines The SkyMines' Plugin
     * @param mineManager A MineManager instance.
     */
    public MineConfigManager(
            SkyMines skyMines,
            MineManager mineManager) {
        this.skyMines = skyMines;
        this.mineManager = mineManager;
    }

    /**
     * Loads all mine config files in the mines folder.
     */
    public void reload() {
        try(Stream<Path> paths = Files.walk(Paths.get(skyMines.getDataFolder() + File.separator + "mines"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        MineConfig mineConfig;
                        @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            mineConfig = loader.load().get(MineConfig.class);
                        } catch (ConfigurateException e) {
                            throw new RuntimeException(e);
                        }

                        if(mineConfig != null) {
                            mineManager.createMine(mineConfig);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
