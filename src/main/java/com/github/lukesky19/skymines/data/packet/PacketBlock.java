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
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains the data for replacing a block server-side, sending the client-side material to display, the loot table to replace for suspicious sand/gravel, and the time the block is on cooldown for.
 * @param worldType The {@link BlockType} of the block that can be mined in the world.
 * @param replacementType The {@link BlockType} to display client-side after a block of the world's {@link BlockType} is broken.
 * @param lootTable An optional {@link LootTable} to replace for suspicious sand or gravel.
 * @param cooldownSeconds The cooldown to apply to the player once the {@link BlockType} has been broken.
 */
public record PacketBlock(@NotNull BlockType worldType, @NotNull BlockType replacementType, @Nullable LootTable lootTable, int cooldownSeconds) {}
