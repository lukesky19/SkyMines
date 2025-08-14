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
package com.github.lukesky19.skymines.manager.bossbar;

import com.github.lukesky19.skymines.data.player.PlayerData;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This class manages the boss bar for players.
 */
public class BossBarManager {
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull MineDataManager mineDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BossBarManager(PlayerDataManager, MineDataManager)} instead.
     * @deprecated You should use You should use {@link #BossBarManager(PlayerDataManager, MineDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BossBarManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param mineDataManager A {@link PlayerDataManager} instance.
     */
    public BossBarManager(@NotNull PlayerDataManager playerDataManager, @NotNull MineDataManager mineDataManager) {
        this.playerDataManager = playerDataManager;
        this.mineDataManager = mineDataManager;
    }

    /**
     * Create and show the boss bar for a particular mine.
     * @param mineId The mine id of the mine.
     * @param player The {@link Player} to show the boss bar.
     * @param uuid The {@link UUID} of the player.
     */
    public void createAndShowBossBar(@NotNull String mineId, @NotNull Player player, @NotNull UUID uuid) {
        AbstractMine mine = mineDataManager.getMineById(mineId);
        if(mine != null) {
            mine.createAndShowBossBar(player, uuid);
        }
    }

    /**
     * Update the boss bar currently shown to the player.
     * @param mineId The mine id of the mine.
     * @param uuid The {@link UUID} of the player.
     */
    public void updateBossBar(@NotNull String mineId, @NotNull UUID uuid) {
        AbstractMine mine = mineDataManager.getMineById(mineId);
        if(mine != null) {
            mine.updateBossBar(uuid);
        }
    }

    /**
     * Set the boss bar currently shown to the player.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @param bossBar The {@link BossBar} to show.
     */
    public void setBossBar(@NotNull Player player, @NotNull UUID uuid, @NotNull BossBar bossBar) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        // Remove the current boss bar shown (if any)
        removeBossBar(player, uuid);

        // Show the new boss bar.
        player.showBossBar(bossBar);
        playerData.setBossBar(bossBar);
    }

    /**
     * Get the {@link BossBar} currently shown to the player or null.
     * @param uuid The {@link UUID} of the player.
     * @return The {@link BossBar} currently shown to the player or null.
     */
    public @Nullable BossBar getBossBar(@NotNull UUID uuid) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        return playerData.getBossBar();
    }

    /**
     * If the player has a boss bar shown from a mine, remove it.
     * @param player The {@link Player} to remove the boss bar from.
     * @param uuid The {@link UUID} of the player.
     */
    public void removeBossBar(@NotNull Player player, @NotNull UUID uuid) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        BossBar bossBar = getBossBar(uuid);
        if(bossBar != null) {
            player.hideBossBar(bossBar);
            playerData.setBossBar(null);
        }
    }
}
