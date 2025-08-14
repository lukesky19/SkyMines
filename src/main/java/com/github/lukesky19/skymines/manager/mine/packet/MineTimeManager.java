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
package com.github.lukesky19.skymines.manager.mine.packet;

import com.github.lukesky19.skymines.data.player.PlayerData;
import com.github.lukesky19.skymines.manager.bossbar.BossBarManager;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class manages the player's time to use mines.
 */
public class MineTimeManager {
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull BossBarManager bossBarManager;

    /**
     * Default Constructor.
     * You should use {@link #MineTimeManager(PlayerDataManager, BossBarManager)} instead.
     * @deprecated You should use You should use {@link #MineTimeManager(PlayerDataManager, BossBarManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public MineTimeManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     */
    public MineTimeManager(@NotNull PlayerDataManager playerDataManager, @NotNull BossBarManager bossBarManager) {
        this.playerDataManager = playerDataManager;
        this.bossBarManager = bossBarManager;
    }

    /**
     * Check if the player has time to access the mine.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to check time for.
     * @return true if the player has time for the provided mine id, otherwise false.
     * Will return false if there is no {@link PlayerData} as well.
     */
    public boolean hasMineTime(@NotNull UUID uuid, @NotNull String mineId) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        return playerData.hasMineTime(mineId);
    }

    /**
     * Get the mine time the player has for the provided mine id.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to get time for.
     * @return The player's time to access the mine. Will return 0 if no {@link PlayerData} exists for the player.
     */
    public long getMineTime(@NotNull UUID uuid, @NotNull String mineId) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        return playerData.getMineTime(mineId);
    }

    /**
     * Adds the time provided in seconds to the player's time to access the mine using the provided mine id.
     * If the player's mine time for the provided mine id is 0, it will be instead set to the time provided.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to increment mine time for.
     * @param timeSeconds The time in seconds to add.
     */
    public void incrementMineTime(@NotNull UUID uuid, @NotNull String mineId, long timeSeconds) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        playerData.incrementMineTime(mineId, timeSeconds);

        bossBarManager.updateBossBar(mineId, uuid);
    }

    /**
     * Removes the time provided in seconds to the player's time to access the mine using the provided mine id.
     * If the player has no time for the provided mine id, the method will just return.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to increment mine time for.
     * @param timeSeconds The time in seconds to add.
     */
    public void decrementMineTime(@NotNull UUID uuid, @NotNull String mineId, long timeSeconds) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        playerData.decrementMineTime(mineId, timeSeconds);

        bossBarManager.updateBossBar(mineId, uuid);
    }

    /**
     * Set the player's time to access the mine using the provided mine id and time seconds.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The id of the mine.
     * @param timeSeconds The time in seconds.
     */
    public void setMineTime(@NotNull UUID uuid, @NotNull String mineId, long timeSeconds) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        playerData.setMineTime(mineId, timeSeconds);

        bossBarManager.updateBossBar(mineId, uuid);
    }
}
