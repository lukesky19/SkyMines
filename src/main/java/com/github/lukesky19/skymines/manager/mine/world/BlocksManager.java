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
package com.github.lukesky19.skymines.manager.mine.world;

import com.github.lukesky19.skymines.data.player.PlayerData;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class manages the blocks players have unlocked for mines.
 */
public class BlocksManager {
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Default Constructor.
     * You should use {@link #BlocksManager(PlayerDataManager)} instead.
     * @deprecated You should use You should use {@link #BlocksManager(PlayerDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public BlocksManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public BlocksManager(@NotNull PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    /**
     * Check if the player has unlocked the block type for the mine id provided.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to check.
     * @param blockType The {@link BlockType} to check.
     * @return true if the player has unlocked access to the block, otherwise false. Will also return false if no data is stored for said mine id.
     */
    public boolean isBlockTypeUnlocked(@NotNull UUID uuid, @NotNull String mineId, @NotNull BlockType blockType) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        return playerData.isBlockTypeUnlocked(mineId, blockType);
    }

    /**
     * Add a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param uuid The {@link UUID} of the player.
     * @param mineId THe mine id to unlock the block for.
     * @param blockType The {@link BlockType}.
     */
    public void addUnlockedBlock(@NotNull UUID uuid, @NotNull String mineId, @NotNull BlockType blockType) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);
        playerData.addUnlockedBlock(mineId, blockType);
    }

    /**
     * Remove a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to lock the block for.
     * @param blockType The {@link BlockType}.
     */
    public void removeUnlockedBlock(@NotNull UUID uuid, @NotNull String mineId, @NotNull BlockType blockType) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);
        playerData.removeUnlockedBlock(mineId, blockType);
    }
}
