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
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens to when a block has already been broken and the items have been calculated and dropped.
 * If the event location occurs within a mine, the event is passed to that mine.
 */
public class BlockDropListener implements Listener {
    private final @NotNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BlockDropListener(MineDataManager)} instead.
     * @deprecated You should use {@link #BlockDropListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlockDropListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public BlockDropListener(@NotNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens for when a block drops an item and if the event occurred inside a mine, the event is passed to said mine.
     * @param blockDropItemEvent A BlockDropItemEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent blockDropItemEvent) {
        AbstractMine mine = mineDataManager.getMineByLocation(blockDropItemEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleBlockDropItem(blockDropItemEvent);
        }
    }
}
