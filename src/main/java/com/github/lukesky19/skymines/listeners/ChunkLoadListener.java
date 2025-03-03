/*
    SkyMines tracks blocks broken in specific regions, replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2025  lukeskywlker19

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

import com.github.lukesky19.skymines.manager.MineManager;
import com.github.lukesky19.skymines.mine.Mine;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * This class listens to when a player loads a chunk and passes that event to the mine they are in (if any).
 */
public class ChunkLoadListener implements Listener {
    private final MineManager mineManager;

    /**
     * Constructor
     * @param mineManager A MineManager instance.
     */
    public ChunkLoadListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Listens to when a player loads a chunk and passes the event to the mine they are in (if any).
     * @param playerChunkLoadEvent A PlayerChunkLoadEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChunkLoad(PlayerChunkLoadEvent playerChunkLoadEvent) {
        Location location = playerChunkLoadEvent.getPlayer().getLocation();

        Mine mine = mineManager.getMineByLocation(location);
        if(mine != null) {
            mine.handlePlayerChunkLoad(playerChunkLoadEvent);
        }
    }
}
