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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens for when an entity changes a block and passes that event to the relevant mine (if any).
 */
public class EntityChangeBlockListener implements Listener {
    private final @NotNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #EntityChangeBlockListener(MineDataManager)} instead.
     * @deprecated You should use {@link #EntityChangeBlockListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public EntityChangeBlockListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public EntityChangeBlockListener(@NotNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens for when an entity changes a block and if that location is inside a mine, pass the event to the mine.
     * @param entityChangeBlockEvent n {@link EntityChangeBlockEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent entityChangeBlockEvent) {
        AbstractMine mine = mineDataManager.getMineByLocation(entityChangeBlockEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleEntityChangeBlockEvent(entityChangeBlockEvent);
        }
    }
}
