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

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymines.gui.FreePreviewGUI;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The config for a world mine.
 * @param configVersion The config version of the file.
 * @param mineId The mine id.
 * @param worldName The world name the mine is for.
 * @param canPlacePlayerBlocks Allow the placing of player placed blocks in mines.
 * @param canBreakPlayerBlocks Allow the mining of player placed blocks.
 * @param restrictPlaceToUnlockedAndFree Restricts the breaking and placing of player blocks to unlocked blocks and free blocks.
 * @param allowPlayerExplosions Allow player initiated explosions to destroy unlocked blocks and free blocks.
 * @param bossBar The boss bar configuration to show while in the mine.
 * @param unlockableBreakable The {@link List} of {@link UnlockBlockData} for the mine.
 * @param freeBreakable A {@link List} of {@link BlockType} for the mine.
 * @param restrictedPlaceable A {@link List} of {@link BlockType} that cannot be placed. Only used if allowBlockPlace is true.
 */
@ConfigSerializable
public record WorldMineConfig(
        @Nullable String configVersion,
        @Nullable String mineId,
        @Nullable String worldName,
        @Nullable Boolean canPlacePlayerBlocks,
        @Nullable Boolean canBreakPlayerBlocks,
        @Nullable Boolean restrictPlaceToUnlockedAndFree,
        @Nullable Boolean allowPlayerExplosions,
        @NotNull BossBarData bossBar,
        @NotNull List<UnlockBlockData> unlockableBreakable,
        @NotNull List<FreeBlockData> freeBreakable,
        @NotNull List<String> restrictedPlaceable) {

    /**
     * This record contains data to populate the shop to purchase access to blocks.
     * @param blockType The {@link BlockType} to purchase access to.
     * @param displayItemLocked The {@link ItemStackConfig} to create the button displayed in the shop when a block is not yet purchased.
     * @param displayItemUnlocked The {@link ItemStackConfig} to create the button displayed in the shop when a block has been unlocked/purchased.
     * @param buyPrice The buy price to unlock the block type.
     */
    @ConfigSerializable
    public record UnlockBlockData(
            @Nullable String blockType,
            @NotNull ItemStackConfig displayItemLocked,
            @NotNull ItemStackConfig displayItemUnlocked,
            @Nullable Double buyPrice) {}

    /**
     * This record contains data to populate the preview gui with free blocks.
     * @param blockType The {@link BlockType} that is free.
     * @param displayItem The {@link ItemStackConfig} to use for the {@link FreePreviewGUI}.
     */
    @ConfigSerializable
    public record FreeBlockData(@Nullable String blockType, @NotNull ItemStackConfig displayItem) {}

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
}
