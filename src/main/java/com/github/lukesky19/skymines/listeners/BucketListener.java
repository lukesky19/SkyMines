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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens to when a player places a bucket's contents or fills a bucket and if that location is inside a mine, the event is passed to that mine.
 */
public class BucketListener implements Listener {
    private final @NotNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BucketListener(MineDataManager)} instead.
     * @deprecated You should use {@link #BucketListener(MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BucketListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public BucketListener(@NotNull MineDataManager mineDataManager) {
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens to when a player fills a bucket and passes the event to the mine the bucket was filled in (if any).
     * @param playerBucketFillEvent A {@link PlayerBucketFillEvent}
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketScoop(PlayerBucketFillEvent playerBucketFillEvent) {
        AbstractMine mine = mineDataManager.getMineByLocation(playerBucketFillEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleBucketFilled(playerBucketFillEvent);
        }
    }

    /**
     * Listens to when a player empties a bucket and passes the event to the mine the bucket was emptied in (if any).
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmptied(PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        AbstractMine mine = mineDataManager.getMineByLocation(playerBucketEmptyEvent.getBlock().getLocation());
        if(mine != null) {
            mine.handleBucketEmptied(playerBucketEmptyEvent);
        }
    }
}
