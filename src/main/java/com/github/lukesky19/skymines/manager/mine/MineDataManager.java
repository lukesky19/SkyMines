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
package com.github.lukesky19.skymines.manager.mine;

import com.github.lukesky19.skymines.mine.AbstractMine;
import com.github.lukesky19.skymines.mine.PacketMine;
import com.github.lukesky19.skymines.mine.WorldMine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class stores mines that have been created by their mine id.
 */
public class MineDataManager {
    private final @NotNull Map<String, AbstractMine> minesMap = new HashMap<>();

    /**
     * Default Constructor.
     */
    public MineDataManager() {}

    /**
     * Add a mine to the {@link Map} of mine ids to {@link AbstractMine}s.
     * @param mineId The mine id.
     * @param mine The {@link AbstractMine}.
     */
    public void addMine(@NotNull String mineId, @NotNull AbstractMine mine) {
        minesMap.put(mineId, mine);
    }

    /**
     * Get a list of all loaded mine ids.
     * @return A {@link List} containing {@link String}s for mine ids.
     */
    @NotNull
    public List<String> getMineIds() {
        return minesMap.keySet().stream().toList();
    }

    /**
     * Get a list of all loaded mine ids that have a time limit associated with them.
     * @return A {@link List} containing {@link String}s for mine ids.
     */
    public @NotNull List<String> getMineIdsWithTime() {
        return minesMap.entrySet().stream().map((entry) -> {
            String mineId = entry.getKey();
            AbstractMine mine = entry.getValue();

            if(mine instanceof PacketMine) {
                return mineId;
            }

            return null;
        }).filter(Objects::nonNull).toList();
    }

    /**
     * Get a list of all loaded mine ids that feature unlocking blocks.
     * @return A {@link List} containing {@link String}s for mine ids.
     */
    public @NotNull List<String> getMineIdsWithBlockUnlocks() {
        return minesMap.entrySet().stream().map((entry) -> {
            String mineId = entry.getKey();
            AbstractMine mine = entry.getValue();

            if(mine instanceof WorldMine) {
                return mineId;
            }

            return null;
        }).filter(Objects::nonNull).toList();
    }

    /**
     * Get a {@link Map} mapping mine ids to {@link AbstractMine}s.
     * @return A {@link Map} mapping mine ids to {@link AbstractMine}s.
     */
    public @NotNull Map<String, AbstractMine> getMinesMap() {
        return minesMap;
    }

    /**
     * Gets a mine by a location.
     * @param location A Bukkit Location
     * @return A mine if the location is inside one, or null.
     */
    public @Nullable AbstractMine getMineByLocation(@NotNull Location location) {
        for(AbstractMine mine : minesMap.values()) {
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
    public AbstractMine getMineById(String id) {
        return minesMap.get(id);
    }

    /**
     * Calls all active Mine's cleanup function and clears the list of active mines.
     * @param onDisable Is the plugin being disabled?
     */
    public void clearMines(boolean onDisable) {
        for(AbstractMine mine : minesMap.values()) {
            mine.cleanUp(onDisable);
        }

        minesMap.clear();
    }
}
