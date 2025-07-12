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
package com.github.lukesky19.skymines.data.config.packet;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The config for a packet mine.
 * @param configVersion The config version of the file.
 * @param mineId The mine id.
 * @param bossBar The boss bar configuration to show while in the mine.
 * @param worldName The world name the mines is in.
 * @param parentRegion The parent region of the mine.
 * @param childRegions The child regions that players can actually mine in.
 */
@ConfigSerializable
public record PacketMineConfig(
        @Nullable String configVersion,
        @Nullable String mineId,
        @NotNull BossBarData bossBar,
        @Nullable String worldName,
        @Nullable String parentRegion,
        @NotNull List<ChildRegionData> childRegions) {

    /**
     * Data for an individual child region that a player can mine in.
     * @param region The region name the player can mine in.
     * @param blocksAllowed A list of blocks the player can mine.
     */
    @ConfigSerializable
    public record ChildRegionData(
            @Nullable String region,
            @NotNull List<BlockData> blocksAllowed) {}

    /**
     * Data for the blocks a player can mine, the replacement material (client-side),
     * the loot table to replace for suspicious sand and gravel, and the cooldown to apply after a player mines a block.
     * @param block The material of the block the player can mine.
     * @param replacement The material to replace the block with client-side.
     * @param lootTable The loot table to replace for suspicious sand and or gravel.
     * @param cooldownSeconds The cooldown before the player can mine the block again.
     */
    @ConfigSerializable
    public record BlockData(
            @Nullable String block,
            @Nullable String replacement,
            @Nullable String lootTable,
            int cooldownSeconds) {}

    /**
     * The data for the boss bar shown to the player.
     * @param timeText The text to show when the player has time for the mine.
     * @param noTimeText The text to show when the player doesn't have time for the mine.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record BossBarData(
            @Nullable String timeText,
            @Nullable String noTimeText,
            @Nullable String color,
            @Nullable String overlay) {}
}
