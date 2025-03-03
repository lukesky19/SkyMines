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
package com.github.lukesky19.skymines.manager;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.loader.LocaleManager;
import com.github.lukesky19.skymines.configuration.record.MineConfig;
import com.github.lukesky19.skymines.mine.Mine;
import com.github.lukesky19.skymines.mine.PacketMine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the management of creating mines, storing active mines, clearing active mines,
 * getting active mines by location or mine id, and getting all loaded mine ids.
 */
public class MineManager {
    private final SkyMines skyMines;
    private final LocaleManager localeManager;
    private final DatabaseManager databaseManager;
    private final List<Mine> minesList = new ArrayList<>();

    /**
     * Constructor
     * @param skyMines The SkyMines' Plugin.
     * @param localeManager A LocaleLoader instance.
     * @param databaseManager A DatabaseManager instance.
     */
    public MineManager(SkyMines skyMines, LocaleManager localeManager, DatabaseManager databaseManager) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.databaseManager = databaseManager;
    }

    /**
     * Creates a new mine and adds it the list of mines if it was created successfully.
     * @param mineConfig The MineConfig for the mine being created.
     */
    public void createMine(@NotNull MineConfig mineConfig) {
        Mine mine = new PacketMine(skyMines, localeManager, databaseManager, mineConfig);

        if(mine.isSetup()) minesList.add(mine);
    }

    /**
     * Calls all active Mine's cleanup function and clears the list of active mines.
     */
    public void clearMines() {
        for(Mine mine : minesList) {
            mine.cleanUp();
        }

        minesList.clear();
    }

    /**
     * Gets a mine by a location.
     * @param location A Bukkit Location
     * @return A mine if the location is inside one, or null.
     */
    @Nullable
    public Mine getMineByLocation(@NotNull Location location) {
        for(Mine mine : minesList) {
            if(mine.isLocationInMine(location)) {
                return mine;
            }
        }

        return null;
    }

    /**
     * Gets a mine by the id of the mine.
     * @param id The id of the mine.
     * @return A mine if the id matches one of the active mines, or null.
     */
    @Nullable
    public Mine getMineById(String id) {
        for(Mine mine : minesList) {
            if(mine.getMineId().equals(id)) return mine;
        }

        return null;
    }

    /**
     * Get a list of all loaded mine ids.
     * @return A non-null List of mine ids.
     */
    @NotNull
    public List<String> getMineIds() {
        return minesList.stream().map(Mine::getMineId).toList();
    }
}
