package com.github.lukesky19.skymines.mine;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Abstract class to extend to create different mines
 */
public abstract class Mine {
    /**
     * Get the identifying name of the mine.
     * @return A String
     */
    @NotNull
    public abstract String getMineId();

    /**
     * Checks if the provided location is inside the mine's parent region.
     * @param location The Location to check.
     * @return true if inside the mine, false if not.
     */
    public abstract boolean isLocationInMine(@NotNull Location location);

    /**
     * Checks if the player can mine the Material at the given location.
     * @param player The player mining the block.
     * @param uuid The UUID of the player.
     * @param location The location of the block.
     * @param material The material of the block.
     * @return true if the player can mine the block, false if not.
     */
    public abstract boolean isBlockMineable(@NotNull Player player, @NotNull UUID uuid, @NotNull Location location, @NotNull Material material);

    /**
     * Checks if a block's Location is on cooldown for the given player's uuid.
     * @param uuid The UUID of the player.
     * @param location The location of the block.
     * @return true if on cooldown, false if not.
     */
    public abstract boolean isBlockOnCooldown(@NotNull UUID uuid, @NotNull Location location);

    /**
     * Handles a BlockBreakEvent.
     * @param blockBreakEvent A BlockBreakEvent
     */
    public abstract void handleBlockBreak(@NotNull BlockBreakEvent blockBreakEvent);

    /**
     * Handles a BlockDropItemEvent.
     * @param blockDropItemEvent A BlockDropItemEvent
     */
    public abstract void handleBlockDropItem(@NotNull BlockDropItemEvent blockDropItemEvent);

    /**
     * Handles a PlayerInteractEvent
     * @param playerInteractEvent A PlayerInteractEvent
     */
    public abstract void handlePlayerInteract(@NotNull PlayerInteractEvent playerInteractEvent);

    /**
     * Handles a PlayerHarvestBlockEvent
     * @param playerHarvestBlockEvent A PlayerHarvestBlockEvent
     */
    public abstract void handlePlayerHarvestBlockEvent(@NotNull PlayerHarvestBlockEvent playerHarvestBlockEvent);

    /**
     * Handles a BlockPlaceEvent
     * @param blockPlaceEvent A BlockPlaceEvent
     */
    public abstract void handleBlockPlace(@NotNull BlockPlaceEvent blockPlaceEvent);

    /**
     * Handles a PlayerMoveEvent
     * @param playerMoveEvent A PlayerMoveEvent
     */
    public abstract void handlePlayerMoveEvent(@NotNull PlayerMoveEvent playerMoveEvent);

    /**
     * Handles a PlayerTeleportEvent
     * @param playerTeleportEvent A PlayerTeleportEvent
     */
    public abstract void handlePlayerTeleportEvent(@NotNull PlayerTeleportEvent playerTeleportEvent);

    /**
     * Handles a PlayerJoinEvent
     * @param playerJoinEvent A PlayerJoinEvent
     */
    public abstract void handlePlayerJoinEvent(@NotNull PlayerJoinEvent playerJoinEvent);

    /**
     * Handles a PlayerQuitEvent
     * @param playerQuitEvent A PlayerQuitEvent
     */
    public abstract void handlePlayerQuitEvent(@NotNull PlayerQuitEvent playerQuitEvent);

    /**
     * Handles a PlayerChunkLoadEvent
     * @param playerChunkLoadEvent A PlayerChunkLoadEvent
     */
    public abstract void handlePlayerChunkLoad(@NotNull PlayerChunkLoadEvent playerChunkLoadEvent);

    /**
     * Adds time for a player to access the mine.
     * @param player The player to give time to.
     * @param uuid The UUID of the player.
     * @param time The amount of time in seconds to add.
     * @return The player's existing time plus the time added.
     */
    public abstract int addPlayerTime(@NotNull Player player, @NotNull UUID uuid, int time);

    /**
     * Removes time for a player to access the mine.
     * @param player The player to give time to.
     * @param uuid The UUID of the player.
     * @param time The amount of time in seconds to add.
     * @return The player's existing time plus the time added.
     */
    public abstract int removePlayerTime(@NotNull Player player, @NotNull UUID uuid, int time);

    /**
     * Sets the time for a player to access the mine.
     * @param player The player to give time to.
     * @param uuid The UUID of the player.
     * @param time The amount of time in seconds to add.
     * @return The player's existing time plus the time added.
     */
    public abstract int setPlayerTime(@NotNull Player player, @NotNull UUID uuid, int time);

    /**
     * Gets the player's time that they can access the mine for.
     * @param uuid The UUID of the player.
     * @return The amount of time the player has in seconds, or null if they have no time.
     */
    @Nullable
    public abstract Integer getPlayerTime(@NotNull UUID uuid);

    /**
     * Cleans up any data necessary when a mine is unloaded.
     */
    public abstract void cleanUp();

    /**
     * Checks if the mine finished being setup without any errors.
     * @return true if setup was successful, false if not.
     */
    public abstract boolean isSetup();
}
