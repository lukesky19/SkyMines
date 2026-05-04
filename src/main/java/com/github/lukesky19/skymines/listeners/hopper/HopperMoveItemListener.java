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
package com.github.lukesky19.skymines.listeners.hopper;

import com.github.lukesky19.skymines.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.interfaces.Mine;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a hopper moves an item, and if that location is inside a mine, the event is passed to that mine.
 */
public class HopperMoveItemListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #HopperMoveItemListener(MineDataManager)} instead.
     * @deprecated You should use {@link #HopperMoveItemListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public HopperMoveItemListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public HopperMoveItemListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a hopper moves an item from one inventory to another and passes the event to the mine the hopper is in (if any).
     * @param inventoryMoveItemEvent A {@link InventoryMoveItemEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent inventoryMoveItemEvent) {
        Location location = inventoryMoveItemEvent.getInitiator().getLocation();
        if(location == null) return;

        Mine mine = mineDataManager.getMineByLocation(location);
        if(mine != null) {
            mine.handleHopperMoveItem(inventoryMoveItemEvent);
        }
    }
}