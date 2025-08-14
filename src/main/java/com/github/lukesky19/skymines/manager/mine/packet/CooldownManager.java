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

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.data.packet.BlockData;
import com.github.lukesky19.skymines.data.player.PlayerData;
import com.github.lukesky19.skymines.manager.player.PlayerDataManager;
import com.github.lukesky19.skymines.util.PluginUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * This class manages the block cooldowns for players.
 */
public class CooldownManager {
    private final @NotNull SkyMines skyMines;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Default Constructor.
     * You should use {@link #CooldownManager(SkyMines, PlayerDataManager)} instead.
     * @deprecated You should use You should use {@link #CooldownManager(SkyMines, PlayerDataManager)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public CooldownManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public CooldownManager(@NotNull SkyMines skyMines, @NotNull PlayerDataManager playerDataManager) {
        this.skyMines = skyMines;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Check if a {@link Location} is on cooldown for the player.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} to check.
     * @return true if on cooldown, otherwise false.
     */
    public boolean isLocationOnCooldown(@NotNull UUID uuid, @NotNull Location location) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        Location cleanLocation = PluginUtils.getCleanLocation(location);

        return playerData.isLocationOnCooldown(cleanLocation);
    }

    /**
     * Add a cooldown for a particular location.
     * @param uuid The {@link UUID} of the player.
     * @param location A {@link Location} add a cooldown for.
     * @param replacementType The {@link BlockType} that is displayed to the client while on cooldown.
     * @param cooldownSeconds The cooldown in seconds.
     */
    public void addLocationCooldown(@NotNull UUID uuid, @NotNull Location location, @NotNull BlockType replacementType, long cooldownSeconds) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        Location cleanLocation = PluginUtils.getCleanLocation(location);

        playerData.addLocationCooldown(cleanLocation, replacementType, cooldownSeconds);
    }

    /**
     * Get a {@link Map} mapping {@link Location}s to {@link BlockData} for locations that are on cooldown for the player.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link Map} mapping {@link Location}s to {@link BlockData}
     */
    public @NotNull Map<Location, BlockData> getBlockDataOnCooldown(@NotNull UUID uuid) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        return playerData.getBlockDataOnCooldown();
    }

    /**
     * Decrement the block's cooldown for the location provided and revet client-side changes if necessary.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location}.
     */
    public void decrementLocationCooldown(@NotNull UUID uuid, @NotNull Location location) {
        @NotNull PlayerData playerData = playerDataManager.getPlayerData(uuid);

        Location cleanLocation = PluginUtils.getCleanLocation(location);

        playerData.decrementLocationCooldown(cleanLocation, 1);

        // Revert client-side block change
        Long updatedCooldown = playerData.getLocationCooldownSeconds(cleanLocation);
        if(updatedCooldown == null || updatedCooldown <= 0) {
            skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                Player player = skyMines.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected() && cleanLocation.isChunkLoaded()) {
                    player.sendBlockChange(cleanLocation, cleanLocation.getBlock().getBlockData());
                }
            }, 1L);
        }
    }
}
