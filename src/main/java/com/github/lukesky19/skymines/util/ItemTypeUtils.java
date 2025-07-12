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
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This class has utilities related to {@link ItemType}s.
 */
public class ItemTypeUtils {
    private static final @NotNull Map<ItemType, BlockType> bucketMapping = Map.of(
            ItemType.AXOLOTL_BUCKET, BlockType.WATER,
            ItemType.COD_BUCKET, BlockType.WATER,
            ItemType.PUFFERFISH_BUCKET, BlockType.WATER,
            ItemType.SALMON_BUCKET, BlockType.WATER,
            ItemType.TADPOLE_BUCKET, BlockType.WATER,
            ItemType.TROPICAL_FISH_BUCKET, BlockType.WATER,
            ItemType.WATER_BUCKET, BlockType.WATER,
            ItemType.POWDER_SNOW_BUCKET, BlockType.POWDER_SNOW,
            ItemType.LAVA_BUCKET, BlockType.LAVA);

    /**
     * Map an {@link ItemType} that is a bucket to the appropriate {@link BlockType} that would be placed.
     * For example, {@link ItemType#AXOLOTL_BUCKET} -> {@link BlockType#WATER}.
     * @param itemType The {@link ItemType} to get the {@link BlockType} for.
     * @return The appropriate {@link BlockType} or null.
     */
    public static @Nullable BlockType mapBucketItemTypeToBlockType(@NotNull ItemType itemType) {
        return bucketMapping.get(itemType);
    }

    /**
     * Is the {@link ItemType} provided that of a hoe?
     * @param itemType The {@link ItemType} to check.
     * @return true if a hoe, otherwise false.
     */
    public static boolean isItemTypeHoe(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.WOODEN_HOE)
                || itemType.equals(ItemType.STONE_HOE)
                || itemType.equals(ItemType.IRON_HOE)
                || itemType.equals(ItemType.GOLDEN_HOE)
                || itemType.equals(ItemType.DIAMOND_HOE)
                || itemType.equals(ItemType.NETHERITE_HOE);
    }

    /**
     * Is the {@link ItemType} provided that of a shovel?
     * @param itemType The {@link ItemType} to check.
     * @return true if a shovel, otherwise false.
     */
    public static boolean isItemTypeShovel(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.WOODEN_SHOVEL)
                || itemType.equals(ItemType.STONE_SHOVEL)
                || itemType.equals(ItemType.IRON_SHOVEL)
                || itemType.equals(ItemType.GOLDEN_SHOVEL)
                || itemType.equals(ItemType.DIAMOND_SHOVEL)
                || itemType.equals(ItemType.NETHERITE_SHOVEL);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#BONE_MEAL}?
     * @param itemType The {@link ItemType} to check.
     * @return true if bone meal, otherwise false.
     */
    public static boolean isItemTypeBoneMeal(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.BONE_MEAL);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#SHEARS}?
     * @param itemType The {@link ItemType} to check.
     * @return true if shears, otherwise false.
     */
    public static boolean isItemTypeShears(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.SHEARS);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#TRIAL_KEY} or {@link ItemType#OMINOUS_TRIAL_KEY}?
     * @param itemType The {@link ItemType} to check.
     * @return true if a trial key or ominous trial key, otherwise false.
     */
    public static boolean isItemTypeKey(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.TRIAL_KEY) || itemType.equals(ItemType.OMINOUS_TRIAL_KEY);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#GLASS_BOTTLE}?
     * @param itemType The {@link ItemType} to check.
     * @return true if a glass bottle, otherwise false.
     */
    public static boolean isItemTypeGlassBottle(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.GLASS_BOTTLE);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#GLOWSTONE}?
     * @param itemType The {@link ItemType} to check.
     * @return true if a glowstone block or false.
     */
    public static boolean isItemTypeGlowstone(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.GLOWSTONE);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#FLINT_AND_STEEL}?
     * @param itemType The {@link ItemType} to check.
     * @return true if a flint and steel, otherwise false.
     */
    public static boolean isItemTypeFlintAndSteel(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.FLINT_AND_STEEL);
    }

    /**
     * Is the {@link ItemType} provided that of a {@link ItemType#FIRE_CHARGE}?
     * @param itemType The {@link ItemType} to check.
     * @return true if a fire charge, otherwise false.
     */
    public static boolean isItemTypeFireCharge(@NotNull ItemType itemType) {
        return itemType.equals(ItemType.FIRE_CHARGE);
    }
}
