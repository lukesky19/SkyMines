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
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class listens to when a player connects to the server, loads their player data, and shows the mine's boss bar if they are inside a mine.
 */
public class PlayerJoinListener implements Listener {
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Default Constructor.
     * You should use {@link #PlayerJoinListener(MineDataManager, PlayerDataManager)} instead.
     * @deprecated You should use {@link #PlayerJoinListener(MineDataManager, PlayerDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public PlayerJoinListener() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param mineDataManager A {@link MineDataManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public PlayerJoinListener(@NotNull MineDataManager mineDataManager, @NotNull PlayerDataManager playerDataManager) {
        this.mineDataManager = mineDataManager;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Listens to when a player logs in and passes the event to the mine they are in (if any).
     * @param playerJoinEvent A PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        playerDataManager.loadPlayerData(uuid).thenAccept(v -> {
            AbstractMine mine = mineDataManager.getMineByLocation(player.getLocation());
            if(mine != null) {
                mine.createAndShowBossBar(player, uuid);
            }
        });
    }
}
