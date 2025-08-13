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
package com.github.lukesky19.skymines.mine;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Abstract class to extend to create different mines
 */
public abstract class AbstractMine {
    /**
     * Default Constructor
     */
    public AbstractMine() {}

    /**
     * Get the identifying name of the mine.
     * @return A String
     */
    public abstract @Nullable String getMineId();

    /**
     * Checks if the provided location is inside the mine's parent region.
     * @param location The Location to check.
     * @return true if inside the mine, false if not.
     */
    public abstract boolean isLocationInMine(@NotNull Location location);

    /**
     * Checks if the player can mine the block at the given location.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} of the block.
     * @param blockType The {@link BlockType} of the block.
     * @return true if the block can be mined, otherwise false.
     */
    public abstract boolean isBlockMineable(@NotNull UUID uuid, @NotNull Location location, @NotNull BlockType blockType);

    /**
     * Checks if a block's Location is on cooldown for the given player's uuid.
     * @param uuid The UUID of the player.
     * @param location The location of the block.
     * @return true if on cooldown, false if not.
     */
    public abstract boolean isLocationOnCooldown(@NotNull UUID uuid, @NotNull Location location);

    /**
     * Handles a {@link BlockBreakEvent}.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    public abstract void handleBlockBreak(@NotNull BlockBreakEvent blockBreakEvent);

    /**
     * Handles a {@link BlockDropItemEvent}.
     * @param blockDropItemEvent A {@link BlockDropItemEvent}.
     */
    public abstract void handleBlockDropItem(@NotNull BlockDropItemEvent blockDropItemEvent);

    /**
     * Handles a {@link PlayerBucketFillEvent}
     * @param playerBucketFillEvent A {@link PlayerBucketFillEvent}
     */
    public abstract void handleBucketFilled(@NotNull PlayerBucketFillEvent playerBucketFillEvent);

    /**
     * Handles a {@link PlayerBucketEmptyEvent}
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}
     */
    public abstract void handleBucketEmptied(@NotNull PlayerBucketEmptyEvent playerBucketEmptyEvent);

    /**
     * Handles a {@link PlayerInteractEvent}.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    public abstract void handlePlayerInteract(@NotNull PlayerInteractEvent playerInteractEvent);

    /**
     * Handles a {@link PlayerHarvestBlockEvent}.
     * @param playerHarvestBlockEvent A {@link PlayerHarvestBlockEvent}.
     */
    public abstract void handlePlayerHarvestBlockEvent(@NotNull PlayerHarvestBlockEvent playerHarvestBlockEvent);

    /**
     * Handles a {@link BlockFertilizeEvent}.
     * @param blockFertilizeEvent A {@link BlockFertilizeEvent}.
     */
    public abstract void handleBlockFertilizeEvent(@NotNull BlockFertilizeEvent blockFertilizeEvent);

    /**
     * Handles a {@link StructureGrowEvent}.
     * @param structureGrowEvent A {@link StructureGrowEvent}.
     */
    public abstract void handleStructureGrowEvent(@NotNull StructureGrowEvent structureGrowEvent);

    /**
     * Handles a {@link EntityChangeBlockEvent}.
     * @param entityChangeBlockEvent A {@link EntityChangeBlockEvent}.
     */
    public abstract void handleEntityChangeBlockEvent(@NotNull EntityChangeBlockEvent entityChangeBlockEvent);

    /**
     * Handles a {@link BlockExplodeEvent}
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param blockExplodeEvent A {@link BlockExplodeEvent}
     */
    public abstract void handleBlockExplodeEvent(@Nullable Player player, @NotNull BlockExplodeEvent blockExplodeEvent);

    /**
     * Handles an {@link EntityExplodeEvent}
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param entityExplodeEvent An {@link EntityExplodeEvent}
     */
    public abstract void handleEntityExplodeEvent(@Nullable Player player, @NotNull EntityExplodeEvent entityExplodeEvent);

    /**
     * Handles a {@link BlockFromToEvent}
     * @param blockFromToEvent A {@link BlockFromToEvent}
     */
    public abstract void handleBlockFromToEvent(@NotNull BlockFromToEvent blockFromToEvent);

    /**
     * Handles a {@link BlockPlaceEvent}.
     * @param blockPlaceEvent A {@link BlockPlaceEvent}.
     */
    public abstract void handleBlockPlace(@NotNull BlockPlaceEvent blockPlaceEvent);

    /**
     * Handles a {@link PlayerMoveEvent}.
     * @param playerMoveEvent A {@link PlayerMoveEvent}.
     */
    public abstract void handlePlayerMoveEvent(@NotNull PlayerMoveEvent playerMoveEvent);

    /**
     * Handles a {@link PlayerTeleportEvent}.
     * @param playerTeleportEvent A {@link PlayerMoveEvent}.
     */
    public abstract void handlePlayerTeleportEvent(@NotNull PlayerTeleportEvent playerTeleportEvent);

    /**
     * Handles a {@link PlayerChunkLoadEvent}.
     * @param playerChunkLoadEvent A {@link PlayerChunkLoadEvent}.
     */
    public abstract void handlePlayerChunkLoad(@NotNull PlayerChunkLoadEvent playerChunkLoadEvent);

    /**
     * Handles the creation and showing of the mine's boss bar to the player.
     * @param player The {@link Player} to show the boss bar to.
     * @param uuid The {@link UUID} of the player.
     */
    public abstract void createAndShowBossBar(@NotNull Player player, @NotNull UUID uuid);

    /**
     * Handles updating of the mine's boss bar currently shown to the player.
     * @param uuid The {@link UUID} of the player.
     */
    public abstract void updateBossBar(@NotNull UUID uuid);

    /**
     * Cleans up any data necessary when a mine is unloaded.
     * @param onDisable Is the plugin being disabled?
     */
    public abstract void cleanUp(boolean onDisable);

    /**
     * Checks if the mine finished being setup without any errors.
     * @return true if setup was successful, false if not.
     */
    public abstract boolean isSetup();
}
