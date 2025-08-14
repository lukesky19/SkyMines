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
package com.github.lukesky19.skymines.manager.mine.world;

import com.github.lukesky19.skylib.libs.morepersistentdatatypes.DataType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages storing data to and reading data from the {@link PersistentDataContainer} on chunks.
 * PDC stands for {@link PersistentDataContainer}.
 */
public class PDCManager {
    /**
     * Default Constructor.
     */
    public PDCManager() {}

    /**
     * Is the block at the {@link Location} provided placed by a player?
     * A water-logged block doesn't count as player-placed, use {@link #isBlockWaterLoggedByPlayer(Location)} for that.
     * You may want to do additional checks with {@link #getPetalCountPlacedByPlayer(Location)} as well.
     * @param location The {@link Location} to check.
     * @return true if player placed, otherwise false.
     */
    public boolean isBlockPlayerPlaced(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_placed");
        List<Location> locationsList = pdc.get(namespacedKey, PersistentDataType.LIST.listTypeFrom(DataType.LOCATION));

        if(locationsList == null || locationsList.isEmpty()) return false;

        return locationsList.contains(location);
    }

    /**
     * Is the block at the {@link Location} provided water-logged by a player?
     * @param location The {@link Location} to check.
     * @return true if player water-logged, otherwise false.
     */
    public boolean isBlockWaterLoggedByPlayer(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_water_logged");
        List<Location> locationsList = pdc.get(namespacedKey, PersistentDataType.LIST.listTypeFrom(DataType.LOCATION));

        if(locationsList == null || locationsList.isEmpty()) return false;

        return locationsList.contains(location);
    }

    /**
     * Get how many petals were placed by a player at the {@link Location}.
     * @param location The {@link Location} to check.
     * @return The amount of petals placed by a player.
     */
    public int getPetalCountPlacedByPlayer(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_petal_count");
        Map<Location, Integer> petalCountByLocation = pdc.get(namespacedKey, DataType.asMap(DataType.LOCATION, PersistentDataType.INTEGER));

        if(petalCountByLocation == null || petalCountByLocation.isEmpty()) return 0;

        return petalCountByLocation.getOrDefault(location, 0);
    }

    /**
     * Mark a {@link Location} as player-placed.
     * This data is stored on the Chunk's {@link PersistentDataContainer}.
     * @param location The {@link Location} to mark as player-placed.
     */
    public void markLocationAsPlayerPlaced(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_placed");
        List<Location> locationsList = new ArrayList<>(pdc.getOrDefault(namespacedKey, PersistentDataType.LIST.listTypeFrom(DataType.LOCATION), new ArrayList<>()));

        locationsList.add(location);

        pdc.set(namespacedKey, PersistentDataType.LIST.listTypeFrom(DataType.LOCATION), locationsList);
    }

    /**
     * Mark a {@link Location} as player water-logged.
     * This data is stored on the Chunk's {@link PersistentDataContainer}.
     * @param location The {@link Location} to mark as player water-logged.
     */
    public void markLocationAsPlayerWaterLogged(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_water_logged");
        List<Location> locationsList = new ArrayList<>(pdc.getOrDefault(namespacedKey, PersistentDataType.LIST.listTypeFrom(DataType.LOCATION), new ArrayList<>()));

        locationsList.add(location);

        pdc.set(namespacedKey, PersistentDataType.LIST.listTypeFrom(DataType.LOCATION), locationsList);
    }

    /**
     * Add 1 to the petal count placed by a player at the provided {@link Location}.
     * @param location The {@link Location} to add to the petal count for.
     */
    public void addPetalCountPlacedByPlayer(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_petal_count");
        Map<Location, Integer> petalCountByLocation = new HashMap<>(pdc.getOrDefault(namespacedKey, DataType.asMap(DataType.LOCATION, PersistentDataType.INTEGER), new HashMap<>()));

        int currentCount = petalCountByLocation.getOrDefault(location, 0);
        currentCount++;
        petalCountByLocation.put(location, currentCount);

        pdc.set(namespacedKey, DataType.asMap(DataType.LOCATION, PersistentDataType.INTEGER), petalCountByLocation);
    }

    /**
     * Remove the stored petal count placed by a player for a particular location.
     * @param location The {@link Location} to remove the petal count for.
     */
    public void clearPetalCountPlacedByPlayer(@NotNull Location location) {
        PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey("skymines", "player_petal_count");
        Map<Location, Integer> petalCountByLocation = new HashMap<>(pdc.getOrDefault(namespacedKey, DataType.asMap(DataType.LOCATION, PersistentDataType.INTEGER), new HashMap<>()));

        petalCountByLocation.remove(location);

        pdc.set(namespacedKey, DataType.asMap(DataType.LOCATION, PersistentDataType.INTEGER), petalCountByLocation);
    }
}
