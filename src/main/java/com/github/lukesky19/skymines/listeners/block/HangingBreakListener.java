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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a player breaks a hanging entity and if that location is inside a mine, the event is passed to that mine.
 */
public class HangingBreakListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #HangingBreakListener(MineDataManager)} instead.
     * @deprecated You should use {@link #HangingBreakListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public HangingBreakListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public HangingBreakListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a hanging entity is broken and passes the event to the mine the entity was broken in (if any).
     * @param hangingBreakEvent A {@link HangingBreakEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent hangingBreakEvent) {
        Mine mine = mineDataManager.getMineByLocation(hangingBreakEvent.getEntity().getLocation());
        if(mine != null) {
            mine.handleHangingBreakEvent(hangingBreakEvent);
        }
    }

    /**
     * Listens to when a hanging entity is broken and passes the event to the mine the entity was broken in (if any).
     * @param hangingBreakByEntityEvent A {@link HangingBreakByEntityEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent hangingBreakByEntityEvent) {
        Mine mine = mineDataManager.getMineByLocation(hangingBreakByEntityEvent.getEntity().getLocation());
        if(mine != null) {
            mine.handleHangingBreakByEntityEvent(hangingBreakByEntityEvent);
        }
    }
}