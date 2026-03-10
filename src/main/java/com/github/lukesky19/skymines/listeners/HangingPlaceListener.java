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
import com.github.lukesky19.skymines.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a player places a hanging entity and if that location is inside a mine, the event is passed to that mine.
 */
public class HangingPlaceListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #HangingPlaceListener(MineDataManager)} instead.
     * @deprecated You should use {@link #HangingPlaceListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public HangingPlaceListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public HangingPlaceListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a hanging entity is placed and passes the event to the mine the entity was broken in (if any).
     * @param hangingPlaceEvent A {@link HangingPlaceEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent hangingPlaceEvent) {
        Mine mine = mineDataManager.getMineByLocation(hangingPlaceEvent.getEntity().getLocation());
        if(mine != null) {
            mine.handleHangingPlace(hangingPlaceEvent);
        }
    }
}