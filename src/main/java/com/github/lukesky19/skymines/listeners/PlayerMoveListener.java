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
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a player moves from one location to another and passes the event to each mine they moved from and to.
 */
public class PlayerMoveListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #PlayerMoveListener(MineDataManager)} instead.
     * @deprecated You should use {@link #PlayerMoveListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public PlayerMoveListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A MineDataManager instance.
     */
    public PlayerMoveListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a player moves from one block to another and passes the event to the mine they are in (if any).
     * @param playerMoveEvent A PlayerMoveEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Location from = playerMoveEvent.getFrom();
        Location to = playerMoveEvent.getTo();

        Location blockFrom = new Location(from.getWorld(), from.getBlockX(), from.getBlockY(), from.getBlockZ());
        Location blockTo = new Location(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());

        Mine fromMine = mineDataManager.getMineByLocation(blockFrom);
        if(fromMine != null) {
            fromMine.handlePlayerMoveEvent(playerMoveEvent);
        }

        Mine toMine = mineDataManager.getMineByLocation(blockTo);
        if(toMine != null) {
            toMine.handlePlayerMoveEvent(playerMoveEvent);
        }
    }
}
