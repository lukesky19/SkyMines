/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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
package com.github.lukesky19.skynodes.listeners;

import com.github.lukesky19.skynodes.manager.NodeManager;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This class manages listening to when a chunk is loaded and sending client-side block updates for tracked blocks.
 */
public class ChunkLoadListener implements Listener {
    final NodeManager nodeManager;

    /**
     * Constructor
     * @param nodeManager NodeManager Instance.
     */
    public ChunkLoadListener(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    /**
     * When a chunk is loaded by a Player, schedule the sending of client-side block updates for tracked blocks.
     * @param event PlayerChunkLoadEvent
     */
    @EventHandler
    public void onChunkLoad(PlayerChunkLoadEvent event) {
        nodeManager.scheduleBulkBlockChange(event.getPlayer(), event.getWorld(), event.getChunk());
    }
}
