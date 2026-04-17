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
package com.github.lukesky19.skymines.data.config.world;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymines.gui.FreePreviewGUI;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The config for a world mine.
 * @param version The config version.
 * @param mineId The mine id.
 * @param worldName The world name the mine is for.
 * @param canPlacePlayerBlocks Allow the placing of player placed blocks in mines.
 * @param canBreakPlayerBlocks Allow the mining of player placed blocks.
 * @param restrictPlaceToUnlockedAndFree Restricts the breaking and placing of player blocks to unlocked blocks and free blocks.
 * @param allowPlayerExplosions Allow player initiated explosions to destroy unlocked blocks and free blocks.
 * @param allowEntityBreak Allow entities like item frames and paintings to be destroyed.
 * @param entityBreakPlayerOnly Only allow players to break entities like item frames and paintings.
 * @param allowEntityPlace Allow entities like item frames and paintings to be placed.
 * @param allowItemFrameItemInsertion Allow players to place items in item frames.
 * @param allowItemFrameItemRemoval Allow players to remove items from item frames.
 * @param allowContainerAccess Allow access to containers? I.e., Chests
 * @param containerAccessPlayerOnly Only allow access to player-placed containers? I.e., Shulkers
 * @param allowHoppers Allow all hoppers to transfer items?
 * @param hoppersPlayerBlocksOnly Only allow player-placed hoppers to move to or remove from player-placed containers.
 * @param bossBar The boss bar configuration to show while in the mine.
 * @param unlockableBreakable The {@link List} of {@link UnlockBlockData} for the mine.
 * @param freeBreakable A {@link List} of {@link BlockType} for the mine.
 * @param restrictedPlaceable A {@link List} of {@link BlockType} that cannot be placed. Only used if allowBlockPlace is true.
 */
@ConfigSerializable
public record WorldMineConfig(
        int version,
        @Nullable String mineId,
        @Nullable String worldName,
        boolean canPlacePlayerBlocks,
        boolean canBreakPlayerBlocks,
        boolean restrictPlaceToUnlockedAndFree,
        boolean allowPlayerExplosions,
        boolean allowEntityPlace,
        boolean allowEntityBreak,
        boolean entityBreakPlayerOnly,
        boolean allowItemFrameItemInsertion,
        boolean allowItemFrameItemRemoval,
        boolean allowContainerAccess,
        boolean containerAccessPlayerOnly,
        boolean allowHoppers,
        boolean hoppersPlayerBlocksOnly,
        @NonNull BossBarData bossBar,
        @NonNull List<UnlockBlockData> unlockableBreakable,
        @NonNull List<FreeBlockData> freeBreakable,
        @NonNull List<BlockType> restrictedPlaceable) {
    /**
     * This record contains data to populate the shop to purchase access to blocks.
     * @param blockType The {@link BlockType} to purchase access to.
     * @param displayItemLocked The {@link ItemStackConfig} to create the button displayed in the shop when a block is not yet purchased.
     * @param displayItemUnlocked The {@link ItemStackConfig} to create the button displayed in the shop when a block has been unlocked/purchased.
     * @param priceData The {@link PriceData} to unlock the block type.
     * @param buyPrice The legacy buy price to unlock the block type. For migration purposes only.
     */
    @ConfigSerializable
    public record UnlockBlockData(
            @Nullable BlockType blockType,
            @NonNull ItemStackConfig displayItemLocked,
            @NonNull ItemStackConfig displayItemUnlocked,
            @NonNull PriceData priceData,
            @Deprecated(since = "3.2.0.0") @Nullable Double buyPrice) {}

    /**
     * This record contains data to populate the preview gui with free blocks.
     * @param blockType The {@link BlockType} that is free.
     * @param displayItem The {@link ItemStackConfig} to use for the {@link FreePreviewGUI}.
     */
    @ConfigSerializable
    public record FreeBlockData(@Nullable BlockType blockType, @NonNull ItemStackConfig displayItem) {}

    /**
     * The data for the boss bar shown to the player while in the mine
     * @param text The text to show when the player is in the mine.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record BossBarData(
            @Nullable String text,
            @Nullable String color,
            @Nullable String overlay) {}

    /**
     * The price data for unlocking a block in the mine.
     * @apiNote The data here is money OR player points, not both.
     * @param money The money required to unlock the block.
     * @param playerPoints The player points required to unlock the block.
     */
    @ConfigSerializable
    public record PriceData(
            @Nullable Double money,
            @Nullable Integer playerPoints) {}
}