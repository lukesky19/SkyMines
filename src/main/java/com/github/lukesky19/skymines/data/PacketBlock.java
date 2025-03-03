package com.github.lukesky19.skymines.data;

import org.bukkit.Material;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains the data for replacing a block server-side, sending the client-side material to display, the loot table to replace for suspicious sand/gravel, and the time the block is on cooldown for.
 * @param worldType The Material Type of the blocks that can be mined in the world.
 * @param replacementType The Material Type to display client-side after a block of the world's Material Type is broken.
 * @param lootTable The loot table to replace for suspicious sand or gravel.
 * @param cooldownSeconds The cooldown before the player can mine the block.
 */
public record PacketBlock(@NotNull Material worldType, @NotNull Material replacementType, @Nullable LootTable lootTable, int cooldownSeconds) {}
