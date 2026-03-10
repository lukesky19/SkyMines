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
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores player data for related to mines.
 */
public class PlayerData {
    // Packet Mines
    private final @NonNull Map<Location, BlockData> blockDataByLocation = new HashMap<>();
    private @NonNull Map<String, Long> mineTimeByMineId = new HashMap<>();

    // World Mines
    private @NonNull Map<String, List<BlockType>> unlockedBlocksByMineId = new HashMap<>();

    // BossBar
    private @Nullable BossBar bossBar;

    // Message Cooldown
    private @Nullable Long messageCooldown;

    /**
     * Default Constructor
     */
    public PlayerData() {}

    /**
     * Constructor
     * @param mineTimeByMineId A {@link Map} mapping mine ids to mine time as a {@link Long}.
     * @param unlockedBlocksByMineId A {@link Map} mapping mine ids to a {@link List} of {@link BlockType}s.
     */
    public PlayerData(@NonNull Map<String, Long> mineTimeByMineId, @NonNull Map<String, List<BlockType>> unlockedBlocksByMineId) {
        this.mineTimeByMineId = mineTimeByMineId;
        this.unlockedBlocksByMineId = unlockedBlocksByMineId;
    }

    /**
     * Check if the player has time to access the mine.
     * @param mineId The mine id to check time for.
     * @return true if the player has time for the provided mine id, otherwise false.
     */
    public boolean hasMineTime(@NonNull String mineId) {
        return mineTimeByMineId.containsKey(mineId);
    }

    /**
     * Get the mine time the player has for the provided mine id.
     * @param mineId The mine id to get time for.
     * @return The player's time to access the mine. If they have no time this returns 0.
     */
    public long getMineTime(@NonNull String mineId) {
        return mineTimeByMineId.getOrDefault(mineId, 0L);
    }

    /**
     * Adds the time provided in seconds to the player's time to access the mine using the provided mine id.
     * If the player's mine time for the provided mine id is 0, it will be instead set to the time provided.
     * @param mineId The mine id to increment mine time for.
     * @param timeSeconds The time in seconds to add.
     */
    public void incrementMineTime(@NonNull String mineId, long timeSeconds) {
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
    public void decrementMineTime(@NonNull String mineId, long timeSeconds) {
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
    public void setMineTime(@NonNull String mineId, long timeSeconds) {
        if(!mineTimeByMineId.containsKey(mineId)) return;

        mineTimeByMineId.put(mineId, timeSeconds);
    }

    /**
     * Get a {@link Map} mapping mine ids to mine time.
     * @return A {@link Map} mapping mine ids to mine time.
     */
    public @NonNull Map<String, Long> getMineTimesByMineIdMap() {
        return mineTimeByMineId;
    }

    /**
     * Check if a {@link Location} is on cooldown for the player.
     * @param location The {@link Location} to check ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @return true if on cooldown, otherwise false.
     */
    public boolean isLocationOnCooldown(@NonNull Location location) {
        Location cleanLocation = PluginUtils.getCleanLocation(location);
        if(!blockDataByLocation.containsKey(cleanLocation)) return false;

        return blockDataByLocation.get(cleanLocation).getCooldownSeconds() > 0;
    }

    /**
     * Get the cooldown in seconds for a particular location.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @return The cooldown in seconds as a {@link Long} or null if no cooldown.
     */
    public @Nullable Long getLocationCooldownSeconds(@NonNull Location location) {
        @Nullable BlockData blockData = blockDataByLocation.get(location);
        if(blockData == null) return null;

        return blockData.getCooldownSeconds();
    }

    /**
     * Get a {@link Map} mapping {@link Location}s to {@link BlockData} for locations that are on cooldown.
     * @return A {@link Map} mapping {@link Location}s to {@link BlockData}
     */
    public @NonNull Map<Location, BlockData> getBlockDataOnCooldown() {
        return blockDataByLocation;
    }

    /**
     * Add a cooldown for a particular location.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @param replacementType The {@link BlockType} that is displayed to the client while on cooldown.
     * @param cooldownSeconds The cooldown in seconds.
     */
    public void addLocationCooldown(@NonNull Location location, @NonNull BlockType replacementType, long cooldownSeconds) {
        BlockData blockData = new BlockData(replacementType, cooldownSeconds);

        blockDataByLocation.put(location, blockData);
    }

    /**
     * Removes the time in seconds provided from a cooldown at the provided {@link Location}.
     * @param location A {@link Location} ran through {@link PluginUtils#getCleanLocation(Location)}.
     * @param timeSeconds The time in seconds to remove.
     */
    public void decrementLocationCooldown(@NonNull Location location, long timeSeconds) {
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
    public boolean isBlockTypeUnlocked(@NonNull String mineId, @NonNull BlockType blockType) {
        @Nullable List<BlockType> unlockedBlocks = unlockedBlocksByMineId.get(mineId);
        if(unlockedBlocks == null) return false;

        return unlockedBlocks.contains(blockType);
    }

    /**
     * Add a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param mineId THe mine id to unlock the block for.
     * @param blockType The {@link BlockType}.
     */
    public void addUnlockedBlock(@NonNull String mineId, @NonNull BlockType blockType) {
        List<BlockType> unlockedBlocks = unlockedBlocksByMineId.computeIfAbsent(mineId, id -> new ArrayList<>());
        unlockedBlocks.add(blockType);
        unlockedBlocksByMineId.put(mineId, unlockedBlocks);
    }

    /**
     * Remove a {@link BlockType} to the list of unlocked blocks for the mine id provided.
     * @param mineId THe mine id to lock the block for.
     * @param blockType The {@link BlockType}.
     */
    public void removeUnlockedBlock(@NonNull String mineId, @NonNull BlockType blockType) {
        @Nullable List<BlockType> unlockedBlocks = unlockedBlocksByMineId.get(mineId);
        if(unlockedBlocks == null) return;

        unlockedBlocks.remove(blockType);
        unlockedBlocksByMineId.put(mineId, unlockedBlocks);
    }

    /**
     * Remove all unlocked blocks for the mine id provided.
     * @param mineId The mine id to remove unlocked blocks for.
     */
    public void removeUnlockedBlocks(@NonNull String mineId) {
        unlockedBlocksByMineId.remove(mineId);
    }

    /**
     * Get a {@link Map} mapping mine ids to a {@link List} of {@link BlockType}s.
     * @return A {@link Map} mapping mine ids to a {@link List} of {@link BlockType}s.
     */
    public @NonNull Map<String, List<BlockType>> getUnlockedBlocksByMineIdMap() {
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

    /**
     * Get the next time the player should be sent a message.
     * @return A {@link Long} to compare against {@link System#currentTimeMillis()} or null.
     */
    public @Nullable Long getMessageCooldown() {
        return messageCooldown;
    }

    /**
     * Set the next time the player should be sent a message.
     * @param messageCooldown A {@link Long} or null.
     */
    public void setMessageCooldown(@Nullable Long messageCooldown) {
        this.messageCooldown = messageCooldown;
    }

    /**
     * Based on the current player's message cooldown, should the player be sent a message?
     * @return true if a message should be sent, or false if on cooldown.
     */
    public boolean shouldSendMessage() {
        if(messageCooldown == null) return true;

        return System.currentTimeMillis() >= messageCooldown;
    }
}