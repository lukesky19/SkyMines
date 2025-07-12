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
package com.github.lukesky19.skymines.util;

import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

/**
 * This class has utilities related to {@link BlockType}s.
 */
public class BlockTypeUtils {
    /**
     * Checks if a {@link BlockType} is that of a bed.
     * @param blockType The {@link BlockType} to check.
     * @return true if a bed, otherwise false.
     */
    public static boolean isBlockTypeBed(@NotNull BlockType blockType) {
        return blockType.equals(BlockType.BLACK_BED)
                || blockType.equals(BlockType.BLUE_BED)
                || blockType.equals(BlockType.BROWN_BED)
                || blockType.equals(BlockType.CYAN_BED)
                || blockType.equals(BlockType.GRAY_BED)
                || blockType.equals(BlockType.GREEN_BED)
                || blockType.equals(BlockType.LIGHT_BLUE_BED)
                || blockType.equals(BlockType.LIGHT_GRAY_BED)
                || blockType.equals(BlockType.LIME_BED)
                || blockType.equals(BlockType.MAGENTA_BED)
                || blockType.equals(BlockType.ORANGE_BED)
                || blockType.equals(BlockType.PINK_BED)
                || blockType.equals(BlockType.PURPLE_BED)
                || blockType.equals(BlockType.RED_BED)
                || blockType.equals(BlockType.WHITE_BED)
                || blockType.equals(BlockType.YELLOW_BED);
    }

    /**
     * Checks if a {@link BlockType} is that of a {@link BlockType#RESPAWN_ANCHOR}
     * @param blockType The {@link BlockType} to check.
     * @return true if a respawn anchor, otherwise false.
     */
    public static boolean isBlockTypeRespawnAnchor(@NotNull BlockType blockType) {
        return blockType.equals(BlockType.BLACK_BED)
                || blockType.equals(BlockType.BLUE_BED)
                || blockType.equals(BlockType.BROWN_BED)
                || blockType.equals(BlockType.CYAN_BED)
                || blockType.equals(BlockType.GRAY_BED)
                || blockType.equals(BlockType.GREEN_BED)
                || blockType.equals(BlockType.LIGHT_BLUE_BED)
                || blockType.equals(BlockType.LIGHT_GRAY_BED)
                || blockType.equals(BlockType.LIME_BED)
                || blockType.equals(BlockType.MAGENTA_BED)
                || blockType.equals(BlockType.ORANGE_BED)
                || blockType.equals(BlockType.PINK_BED)
                || blockType.equals(BlockType.PURPLE_BED)
                || blockType.equals(BlockType.RED_BED)
                || blockType.equals(BlockType.WHITE_BED)
                || blockType.equals(BlockType.YELLOW_BED);
    }
}
