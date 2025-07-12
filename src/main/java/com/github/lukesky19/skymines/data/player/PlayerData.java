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
package com.github.lukesky19.skymines.data.player;

import com.github.lukesky19.skymines.data.packet.BlockData;
import com.github.lukesky19.skymines.util.PluginUtils;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores player data for related to mines.
 */
public class PlayerData {
    // Packet Mines
    private final @NotNull Map<Location, BlockData> blockDataByLocation = new HashMap<>();
    private @NotNull Map<String, Long> mineTimeByMineId = new HashMap<>();

    // World Mines
    private @NotNull Map<String, List<BlockType>> unlockedBlocksByMineId = new HashMap<>();

    // Other
    private @Nullable BossBar bossBar;

    /**
     * Default Constructor
     */
    public PlayerData() {}

    /**
     * Constructor
     * @param mineTimeByMineId A {@link Map} mapping mine ids to mine time as a {@link Long}.
     * @param unlockedBlocksByMineId A {@link Map} mapping mine ids to a {@link List} of {@link BlockType}s.
     */
    public PlayerData(@NotNull Map<String, Long> mineTimeByMineId, @NotNull Map<String, List<BlockType>> unlockedBlocksByMineId) {
        this.mineTimeByMineId = mineTimeByMineId;
        this.unlockedBlocksByMineId = unlockedBlocksByMineId;
    }

    /**
     * Check if the player has time to access the mine.
     * @param mineId The mine id to check time for.
     * @return true if the player has time for the provided mine id, otherwise false.
     */
    public boolean hasMineTime(@NotNull String mineId) {
        return mineTimeByMineId.containsKey(mineId);
    }

    /**
     * Get the mine time the player has for the provided mine id.
     * @param mineId The mine id to get time for.
     * @return The player's time to access the mine. If they have no time this returns 0.
     */
    public long getMineTime(@NotNull String mineId) {
        return mineTimeByMineId.getOrDefault(mineId, 0L);
    }

    /**
     * Adds the time provided in seconds to the player's time to access the mine using the provided mine id.
     * If the player's mine time for the provided mine id is 0, it will be instead set to the time provided.
     * @param mineId The mine id to increment mine time for.
     * @param timeSeconds The time in seconds to add.
     */
    public void incrementMineTime(@NotNull String mineId, long timeSeconds) {
        if(mineTimeByMineId.containsKey(mineId)) {
            mineTimeByMineId.put(mineId, (mineTimeByMineId.get(mineId) + timeSeconds));
        } else {
            mineTimeByMineId.put(mineId, timeSeconds);
        }
    }

    /**
     * Removes the time provided in seconds to the player's time to access the mine using the provided mine id.
     * If the player has no time for the provided mine id, the method will just return.
     * If the updated mine time is less than or equal to 0, it will be removed from the mine time map.
     * @param mineId The mine id to increment mine time for.
     * @param timeSeconds The time in seconds to add.
     */
    public void decrementMineTime(@NotNull String mineId, long timeSeconds) {
        if(!mineTimeByMineId.containsKey(mineId)) return;

        // Calculate the updated time
        long updatedTime = mineTimeByMineId.get(mineId) - timeSeconds;

        // If the updated time is less than or equal to 0, remove the time from the Map and return
        if(updatedTime <= 0) {
            mineTimeByMineId.remove(mineId);
            return;
        }

        // Insert the updated time if it is greater than 0
        mineTimeByMineId.put(mineId, updatedTime);
    }

    /**
     * Set the time to access the mine using the provided mine id and time seconds.
     * @param mineId The id of the mine.
     * @param timeSeconds The time in seconds.
     */
    public void setMineTime(@NotNull String mineId, long timeSeconds) {
        if(!mineTimeByMineId.containsKey(mineId)) return;

        mineTimeByMineId.put(mineId, timeSeconds);
    }

    /**
     * Get a {@link Map} mapping mine ids to mine time.
     * @return A {@link Map} mapping mine ids to mine time.
     */
    public @NotNull Map<String, Long> getMineTimesByMineIdMap() {
        return mineTimeByMineId;
    }

    /**
     * Check if a {@link Location} is on cooldown for the player.
     * @param location The {@link Location} to check ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @return true if on cooldown, otherwise false.
     */
    public boolean isLocationOnCooldown(@NotNull Location location) {
        Location cleanLocation = PluginUtils.getCleanLocation(location);
        if(!blockDataByLocation.containsKey(cleanLocation)) return false;

        return blockDataByLocation.get(cleanLocation).getCooldownSeconds() > 0;
    }

    /**
     * Get the cooldown in seconds for a particular location.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @return The cooldown in seconds as a {@link Long} or null if no cooldown.
     */
    public @Nullable Long getLocationCooldownSeconds(@NotNull Location location) {
        @Nullable BlockData blockData = blockDataByLocation.get(location);
        if(blockData == null) return null;

        return blockData.getCooldownSeconds();
    }

    /**
     * Get the {@link BlockType} to display to the client while on cooldown.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     */
    public @Nullable BlockType getLocationReplacementType(@NotNull Location location) {
        @Nullable BlockData blockData = blockDataByLocation.get(location);
        if(blockData == null) return null;

        return blockData.getReplacementType();
    }

    /**
     * Get a {@link Map} mapping {@link Location}s to {@link BlockData} for locations that are on cooldown.
     * @return A {@link Map} mapping {@link Location}s to {@link BlockData}
     */
    public @NotNull Map<Location, BlockData> getBlockDataOnCooldown() {
        return blockDataByLocation;
    }

    /**
     * Add a cooldown for a particular location.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @param replacementType The {@link BlockType} that is displayed to the client while on cooldown.
     * @param cooldownSeconds The cooldown in seconds.
     */
    public void addLocationCooldown(@NotNull Location location, @NotNull BlockType replacementType, long cooldownSeconds) {
        BlockData blockData = new BlockData(replacementType, cooldownSeconds);

        blockDataByLocation.put(location, blockData);
    }

    /**
     * Remove a cooldown for a particular location.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     */
    public void removeLocationCooldown(@NotNull Location location) {
        blockDataByLocation.remove(location);
    }

    /**
     * Removes the time in seconds provided from a cooldown at the provided {@link Location}.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @param timeSeconds The time in seconds to remove.
     */
    public void decrementLocationCooldown(@NotNull Location location, long timeSeconds) {
        if(!blockDataByLocation.containsKey(location)) return;
        BlockData blockData = blockDataByLocation.get(location);

        long cooldownSeconds = blockData.getCooldownSeconds();
        cooldownSeconds -= timeSeconds;

        if(cooldownSeconds > 0) {
            blockData.setCooldownSeconds(cooldownSeconds);
        } else {
            blockDataByLocation.remove(location);
        }
    }

    /**
     * Check if the player has unlocked the block type for the mine id provided.
     * @param mineId The mine id to check.
     * @param blockType The {@link BlockType} to check.
     * @return true if the player has unlocked access to the block, otherwise false. Will also return false if no data is stored for said mine id.
     */
    public boolean isBlockTypeUnlocked(@NotNull String mineId, @NotNull BlockType blockType) {
        @Nullable List<BlockType> unlockedBlocks = unlockedBlocksByMineId.get(mineId);
        if(unlockedBlocks == null) return false;

        return unlockedBlocks.contains(blockType);
    }

    /**
     * Add a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param mineId THe mine id to unlock the block for.
     * @param blockType The {@link BlockType}.
     * @return true if added successfully, otherwise false.
     */
    public boolean addUnlockedBlock(@NotNull String mineId, @NotNull BlockType blockType) {
        List<BlockType> unlockedBlocks = unlockedBlocksByMineId.getOrDefault(mineId, new ArrayList<>());

        unlockedBlocks.add(blockType);
        unlockedBlocksByMineId.put(mineId, unlockedBlocks);

        return true;
    }

    /**
     * Remove a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param mineId THe mine id to lock the block for.
     * @param blockType The {@link BlockType}.
     * @return true if removed successfully, otherwise false.
     */
    public boolean removeUnlockedBlock(@NotNull String mineId, @NotNull BlockType blockType) {
        @Nullable List<BlockType> unlockedBlocks = unlockedBlocksByMineId.get(mineId);
        if(unlockedBlocks == null) return false;

        unlockedBlocks.remove(blockType);
        unlockedBlocksByMineId.put(mineId, unlockedBlocks);

        return true;
    }

    /**
     * Get a {@link Map} mapping mine ids to a {@link List} of {@link BlockType}s.
     * @return A {@link Map} mapping mine ids to a {@link List} of {@link BlockType}s.
     */
    public @NotNull Map<String, List<BlockType>> getUnlockedBlocksByMineIdMap() {
        return unlockedBlocksByMineId;
    }

    /**
     * Set the active {@link BossBar} being displayed to the player.
     * @param bossBar The {@link BossBar} to store, may be null.
     */
    public void setBossBar(@Nullable BossBar bossBar) {
        this.bossBar = bossBar;
    }

    /**
     * Get the active {@link BossBar} being displayed to the player.
     * @return The {@link BossBar} being displayed to the player. May be null.
     */
    public @Nullable BossBar getBossBar() {
        return this.bossBar;
    }
}
