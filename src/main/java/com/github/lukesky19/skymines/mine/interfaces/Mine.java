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
package com.github.lukesky19.skymines.mine.interfaces;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Abstract class to extend to create different mines
 */
public interface Mine {
    /**
     * Get the identifying name of the mine.
     * @return A String
     */
    @Nullable String getMineId();

    /**
     * Checks if the provided location is inside the mine's parent region.
     * @param location The Location to check.
     * @return true if inside the mine, false if not.
     */
    boolean isLocationInMine(@NonNull Location location);

    /**
     * Checks if the player can mine the block at the given location.
     * @param player The {@link Player}.
     * @param location The {@link Location} of the block.
     * @param blockType The {@link BlockType} of the block.
     * @return true if the block can be mined, otherwise false.
     */
    boolean isBlockMineable(@NonNull Player player, @NonNull Location location, @NonNull BlockType blockType);

    /**
     * Checks if a block's Location is on cooldown for the given player's uuid.
     * @param uuid The UUID of the player.
     * @param location The location of the block.
     * @return true if on cooldown, false if not.
     */
    boolean isLocationOnCooldown(@NonNull UUID uuid, @NonNull Location location);

    /**
     * Handles a {@link BlockBreakEvent}.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    void handleBlockBreak(@NonNull BlockBreakEvent blockBreakEvent);

    /**
     * Handles a {@link BlockDropItemEvent}.
     * @param blockDropItemEvent A {@link BlockDropItemEvent}.
     */
    void handleBlockDropItem(@NonNull BlockDropItemEvent blockDropItemEvent);

    /**
     * Handles a {@link PlayerBucketFillEvent}
     * @param playerBucketFillEvent A {@link PlayerBucketFillEvent}
     */
    void handleBucketFilled(@NonNull PlayerBucketFillEvent playerBucketFillEvent);

    /**
     * Handles a {@link PlayerBucketEmptyEvent}
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}
     */
    void handleBucketEmptied(@NonNull PlayerBucketEmptyEvent playerBucketEmptyEvent);

    /**
     * Handles a {@link PlayerInteractEvent}.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    void handlePlayerInteract(@NonNull PlayerInteractEvent playerInteractEvent);

    /**
     * Handles a {@link PlayerHarvestBlockEvent}.
     * @param playerHarvestBlockEvent A {@link PlayerHarvestBlockEvent}.
     */
    void handlePlayerHarvestBlockEvent(@NonNull PlayerHarvestBlockEvent playerHarvestBlockEvent);

    /**
     * Handles a {@link BlockFertilizeEvent}.
     * @param blockFertilizeEvent A {@link BlockFertilizeEvent}.
     */
    void handleBlockFertilizeEvent(@NonNull BlockFertilizeEvent blockFertilizeEvent);

    /**
     * Handles a {@link StructureGrowEvent}.
     * @param structureGrowEvent A {@link StructureGrowEvent}.
     */
    void handleStructureGrowEvent(@NonNull StructureGrowEvent structureGrowEvent);

    /**
     * Handles an {@link InventoryMoveItemEvent}.
     * @param inventoryMoveItemEvent An {@link InventoryMoveItemEvent}.
     */
    void handleHopperMoveItem(@NonNull InventoryMoveItemEvent inventoryMoveItemEvent);

    /**
     * Handles a {@link EntityChangeBlockEvent}.
     * @param entityChangeBlockEvent An {@link EntityChangeBlockEvent}.
     */
    void handleEntityChangeBlockEvent(@NonNull EntityChangeBlockEvent entityChangeBlockEvent);

    /**
     * Handles a {@link HangingPlaceEvent}.
     * @param hangingPlaceEvent An {@link HangingPlaceEvent}.
     */
    void handleHangingPlace(@NonNull HangingPlaceEvent hangingPlaceEvent);

    /**
     * Handles a {@link HangingBreakEvent}.
     * @param hangingBreakEvent A {@link HangingBreakEvent}.
     */
    void handleHangingBreakEvent(@NonNull HangingBreakEvent hangingBreakEvent);

    /**
     * Handles a {@link HangingBreakByEntityEvent}.
     * @param hangingBreakByEntityEvent A {@link HangingBreakByEntityEvent}.
     */
    void handleHangingBreakByEntityEvent(@NonNull HangingBreakByEntityEvent hangingBreakByEntityEvent);

    /**
     * Handles a {@link PlayerItemFrameChangeEvent}.
     * @param playerItemFrameChangeEvent A {@link PlayerItemFrameChangeEvent}.
     */
    void handlePlayerItemFrameChangeEvent(@NonNull PlayerItemFrameChangeEvent playerItemFrameChangeEvent);

    /**
     * Handles a {@link BlockExplodeEvent}
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param blockExplodeEvent A {@link BlockExplodeEvent}
     */
    void handleBlockExplodeEvent(@Nullable Player player, @NonNull BlockExplodeEvent blockExplodeEvent);

    /**
     * Handles an {@link EntityExplodeEvent}
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param entityExplodeEvent An {@link EntityExplodeEvent}
     */
    void handleEntityExplodeEvent(@Nullable Player player, @NonNull EntityExplodeEvent entityExplodeEvent);

    /**
     * Handles a {@link BlockFromToEvent}
     * @param blockFromToEvent A {@link BlockFromToEvent}
     */
    void handleBlockFromToEvent(@NonNull BlockFromToEvent blockFromToEvent);

    /**
     * Handles a {@link BlockPlaceEvent}.
     * @param blockPlaceEvent A {@link BlockPlaceEvent}.
     */
    void handleBlockPlace(@NonNull BlockPlaceEvent blockPlaceEvent);

    /**
     * Handles a {@link PlayerMoveEvent}.
     * @param playerMoveEvent A {@link PlayerMoveEvent}.
     */
    void handlePlayerMoveEvent(@NonNull PlayerMoveEvent playerMoveEvent);

    /**
     * Handles a {@link PlayerTeleportEvent}.
     * @param playerTeleportEvent A {@link PlayerMoveEvent}.
     */
    void handlePlayerTeleportEvent(@NonNull PlayerTeleportEvent playerTeleportEvent);

    /**
     * Handles a {@link PlayerChunkLoadEvent}.
     * @param playerChunkLoadEvent A {@link PlayerChunkLoadEvent}.
     */
    void handlePlayerChunkLoad(@NonNull PlayerChunkLoadEvent playerChunkLoadEvent);

    /**
     * Handles the creation and showing of the mine's boss bar to the player.
     * @param player The {@link Player} to show the boss bar to.
     * @param uuid The {@link UUID} of the player.
     */
    void createAndShowBossBar(@NonNull Player player, @NonNull UUID uuid);

    /**
     * Handles updating of the mine's boss bar currently shown to the player.
     * @param uuid The {@link UUID} of the player.
     */
    void updateBossBar(@NonNull UUID uuid);

    /**
     * Cleans up any data necessary when a mine is unloaded.
     * @param onDisable Is the plugin being disabled?
     */
    void cleanUp(boolean onDisable);

    /**
     * Checks if the mine finished being setup without any errors.
     * @return true if setup was successful, false if not.
     */
    boolean isSetup();
}