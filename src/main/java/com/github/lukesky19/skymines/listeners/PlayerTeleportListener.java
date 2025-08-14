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
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * This class listens to when a player teleports and passes the event to the mine the player was in and is now in (if any).
 */
public class PlayerTeleportListener implements Listener {
    private final MineDataManager mineDataManager;

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public PlayerTeleportListener(MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a player teleports and passes the event to the mine the teleport events occurred in (if any)
     * @param playerTeleportEvent A PlayerTeleportEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent playerTeleportEvent) {
        AbstractMine fromMine = mineDataManager.getMineByLocation(playerTeleportEvent.getFrom());
        if(fromMine != null) {
            fromMine.handlePlayerTeleportEvent(playerTeleportEvent);
        }

        AbstractMine toMine = mineDataManager.getMineByLocation(playerTeleportEvent.getTo());
        if(toMine != null) {
            toMine.handlePlayerTeleportEvent(playerTeleportEvent);
        }
    }
}
