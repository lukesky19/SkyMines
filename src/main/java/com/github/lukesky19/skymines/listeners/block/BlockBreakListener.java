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
import org.bukkit.event.block.BlockBreakEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a player breaks a block and if that location is inside a mine, the event is passed to that mine.
 */
public class BlockBreakListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BlockBreakListener(MineDataManager)} instead.
     * @deprecated You should use {@link #BlockBreakListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlockBreakListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A MineDataManager instance.
     */
    public BlockBreakListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a player breaks a block and passes the event to the mine the block was broken in (if any).
     * @param blockBreakEvent A BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        Mine mine = mineDataManager.getMineByLocation(blockBreakEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleBlockBreak(blockBreakEvent);
        }
    }
}