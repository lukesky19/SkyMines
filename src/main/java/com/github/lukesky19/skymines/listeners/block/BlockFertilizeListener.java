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
package com.github.lukesky19.skymines.listeners.block;

import com.github.lukesky19.skymines.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.interfaces.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a structure is grown and passes the event to a mine (if any).
 */
public class BlockFertilizeListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BlockFertilizeListener(MineDataManager)} instead.
     * @deprecated You should use {@link #BlockFertilizeListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlockFertilizeListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public BlockFertilizeListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens for when a block is fertilized and if that location is inside a mine, pass the event to the mine.
     * @param blockFertilizeEvent A {@link BlockFertilizeEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(BlockFertilizeEvent blockFertilizeEvent) {
        Mine mine = mineDataManager.getMineByLocation(blockFertilizeEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleBlockFertilizeEvent(blockFertilizeEvent);
        }
    }
}
