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
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to when a player interacts with an item frame inside a mine and passes the event to that mine.
 */
public class ItemFrameChangeListener implements Listener {
    private final @NonNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #ItemFrameChangeListener(MineDataManager)} instead.
     * @deprecated You should use {@link #ItemFrameChangeListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public ItemFrameChangeListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public ItemFrameChangeListener(@NonNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when an item frame is interacted with and passes the event to the mine the item frame is in (if any).
     * @param playerItemFrameChangeEvent A {@link PlayerItemFrameChangeEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameChange(PlayerItemFrameChangeEvent playerItemFrameChangeEvent) {
        Mine mine = mineDataManager.getMineByLocation(playerItemFrameChangeEvent.getItemFrame().getLocation());
        if(mine != null) {
            mine.handlePlayerItemFrameChangeEvent(playerItemFrameChangeEvent);
        }
    }
}