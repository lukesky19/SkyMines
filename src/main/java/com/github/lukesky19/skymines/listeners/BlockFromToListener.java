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
package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for when a block moves from one location to another (i.e., water and lava).
 */
public class BlockFromToListener implements Listener {
    private final @NotNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BlockFromToListener(MineDataManager)} instead.
     * @deprecated You should use {@link #BlockFromToListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlockFromToListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public BlockFromToListener(@NotNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens for when a block moves from one location to another and if that location is inside a mine, pass the event to the mine.
     * @param blockFromToEvent A {@link BlockFromToEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent blockFromToEvent) {
        AbstractMine mine = mineDataManager.getMineByLocation(blockFromToEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleBlockFromToEvent(blockFromToEvent);
        }
    }
}
