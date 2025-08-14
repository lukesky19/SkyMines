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
package com.github.lukesky19.skymines.data.packet;

import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

/**
 * This class contains the data for a block that is on cooldown.
 */
public class BlockData {
    private final @NotNull BlockType replacementType;
    private long cooldownSeconds;

    /**
     * Constructor
     * @param replacementType The material that replaced it client-side.
     * @param cooldownSeconds The starting cooldown for this block.
     */
    public BlockData(@NotNull BlockType replacementType, long cooldownSeconds) {
        this.replacementType = replacementType;
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * The {@link BlockType} to display to the client when the block is on cooldown.
     * @return A {@link BlockType}.
     */
    public @NotNull BlockType getReplacementType() {
        return replacementType;
    }

    /**
     * Set the seconds that this block is on cooldown for.
     * @param cooldownSeconds The cooldown for the block in seconds.
     */
    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Get the remaining seconds that the block is on cooldown for.
     * @return The cooldown in seconds.
     */
    public long getCooldownSeconds() {
        return cooldownSeconds;
    }
}
