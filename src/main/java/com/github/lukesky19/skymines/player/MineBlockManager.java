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
package com.github.lukesky19.skymines.player;

import com.github.lukesky19.skymines.integration.HookManager;
import com.github.lukesky19.skymines.integration.hooks.LuckPermsHook;
import com.github.lukesky19.skymines.player.data.PlayerData;
import com.github.lukesky19.skymines.settings.SettingsManager;
import com.github.lukesky19.skymines.settings.data.Settings;
import com.github.lukesky19.skymines.util.enums.BlockUnlockResult;
import net.luckperms.api.model.user.User;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * This class manages the blocks players have unlocked for mines.
 */
public class MineBlockManager {
    private final @NonNull SettingsManager settingsManager;
    private final @NonNull PlayerDataManager playerDataManager;
    private final @NonNull HookManager hookManager;

    /**
     * Default Constructor.
     * You should use {@link #MineBlockManager(SettingsManager, PlayerDataManager, HookManager)} instead.
     * @deprecated You should use You should use {@link #MineBlockManager(SettingsManager, PlayerDataManager, HookManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public MineBlockManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public MineBlockManager(@NonNull SettingsManager settingsManager, @NonNull PlayerDataManager playerDataManager, @NonNull HookManager hookManager) {
        this.settingsManager = settingsManager;
        this.playerDataManager = playerDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Check if the player has unlocked the block type for the mine id provided.
     * @param player The {@link Player}.
     * @param mineId The mine id to check.
     * @param blockType The {@link BlockType} to check.
     * @return true if the player has unlocked access to the block, otherwise false. Will also return false if no data is stored for said mine id.
     */
    public boolean isBlockTypeUnlocked(@NonNull Player player, @NonNull String mineId, @NonNull BlockType blockType) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        return playerData.isBlockTypeUnlocked(mineId, blockType);
    }

    /**
     * Add the {@link BlockType} to the list of unlocked blocks for the mine id and player provided.
     * @param player The {@link Player}.
     * @param mineId THe mine id to unlock the block for.
     * @param blockType The {@link BlockType}.
     * @return A {@link BlockUnlockResult}.
     */
    public @NonNull BlockUnlockResult addUnlockedBlock(@NonNull Player player, @NonNull String mineId, @NonNull BlockType blockType) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) return BlockUnlockResult.INVALID_SETTINGS;

        if(!settings.blockUnlockExcludedGroups().isEmpty()) {
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            if(luckPermsHook.isHooked()) {
                User user = luckPermsHook.getUser(player);
                if(user == null) return BlockUnlockResult.INVALID_USER;

                boolean isExcluded = settings.blockUnlockExcludedGroups().stream()
                        .filter(groupConfig -> groupConfig.groupName() != null)
                        .anyMatch(groupConfig -> luckPermsHook.hasInheritanceNode(user, groupConfig.groupName(), groupConfig.contexts()));
                if(isExcluded) return BlockUnlockResult.PLAYER_EXCLUDED;
            }
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        playerData.addUnlockedBlock(mineId, blockType);

        return BlockUnlockResult.SUCCESS;
    }

    /**
     * Remove a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param uuid The {@link UUID} of the player.
     * @param mineId The mine id to lock the block for.
     * @param blockType The {@link BlockType}.
     */
    public void removeUnlockedBlock(@NonNull UUID uuid, @NonNull String mineId, @NonNull BlockType blockType) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        playerData.removeUnlockedBlock(mineId, blockType);
    }

    /**
     * Remove all unlocked blocks for the player id and mine id provided.
     * @param uuid The player's {@link UUID}.
     * @param mineId The mine id.
     */
    public void removeUnlockedBlocks(@NonNull UUID uuid, @NonNull String mineId) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        playerData.removeUnlockedBlocks(mineId);
    }
}
