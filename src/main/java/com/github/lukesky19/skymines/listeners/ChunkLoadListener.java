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
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens to when a player loads a chunk and passes that event to the mine they are in (if any).
 */
public class ChunkLoadListener implements Listener {
    private final @NotNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #ChunkLoadListener(MineDataManager)} instead.
     * @deprecated You should use {@link #ChunkLoadListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public ChunkLoadListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A MineDataManager instance.
     */
    public ChunkLoadListener(@NotNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a player loads a chunk and passes the event to the mine they are in (if any).
     * @param playerChunkLoadEvent A PlayerChunkLoadEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChunkLoad(PlayerChunkLoadEvent playerChunkLoadEvent) {
        Location location = playerChunkLoadEvent.getPlayer().getLocation();

        AbstractMine mine = mineDataManager.getMineByLocation(location);
        if(mine != null) {
            mine.handlePlayerChunkLoad(playerChunkLoadEvent);
        }
    }
}
