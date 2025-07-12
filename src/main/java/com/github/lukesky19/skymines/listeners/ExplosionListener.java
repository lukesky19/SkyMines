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
package com.github.lukesky19.skymines.listeners;

import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.manager.mine.MineDataManager;
import com.github.lukesky19.skymines.mine.AbstractMine;
import com.github.lukesky19.skymines.util.BlockTypeUtils;
import com.github.lukesky19.skymines.util.ItemTypeUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class listens to {@link PlayerInteractEvent}, {@link PlayerInteractEntityEvent}, {@link EntitySpawnEvent},
 * {@link BlockExplodeEvent} and {@link EntityExplodeEvent} to determine if a player initiated the explosions.
 */
public class ExplosionListener implements Listener {
    private final @NotNull SkyMines skyMines;
    private final @NotNull MineDataManager mineDataManager;
    private final @NotNull Map<UUID, List<UUID>> playerInitiatedEntityExplosions = new HashMap<>();
    private final @NotNull Map<UUID, List<Location>> playerInitiatedBlockExplosions = new HashMap<>();

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param mineDataManager A {@link MineDataManager} instance.
     */
    public ExplosionListener(@NotNull SkyMines skyMines, @NotNull MineDataManager mineDataManager) {
        this.skyMines = skyMines;
        this.mineDataManager = mineDataManager;
    }

    /**
     * Listens for when a player interacts with a block that will initiate a {@link BlockExplodeEvent} or an {@link EntitySpawnEvent}.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractBlock(PlayerInteractEvent playerInteractEvent) {
        // Player
        Player player = playerInteractEvent.getPlayer();
        UUID playerId = player.getUniqueId();
        // Action
        Action action =  playerInteractEvent.getAction();
        if(!action.equals(Action.RIGHT_CLICK_BLOCK)) return;
        // Environment
        World.Environment environment = player.getWorld().getEnvironment();
        // Block
        Block block = playerInteractEvent.getClickedBlock();
        if(block == null) return;
        // BlockType
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;
        // Location
        Location location = block.getLocation();
        // ItemStack used
        ItemStack handItem = playerInteractEvent.getItem();

        // Consider the Environment
        switch(environment) {
            case NORMAL -> {
                // If the block interacted with is a respawn anchor, and they are holding glowstone, store the player uuid and location where the explosion is expected.
                if(handItem != null) {
                    ItemType itemType = handItem.getType().asItemType();
                    if(itemType != null) {
                        if(BlockTypeUtils.isBlockTypeRespawnAnchor(blockType) && isRespawnAnchorChargesMax(block) && ItemTypeUtils.isItemTypeGlowstone(itemType)) {
                            addLocationToPlayerInitiatedBlockExplosions(playerId, location);
                        }
                    }
                }
            }

            case NETHER -> {
                // If the block is a bed, store the player uuid and location where the explosion is expected.
                if(BlockTypeUtils.isBlockTypeBed(blockType)) {
                    addLocationToPlayerInitiatedBlockExplosions(playerId, location);
                }
            }

            case THE_END -> {
                // If the block is a bed, store the player uuid and location where the explosion is expected.
                if(BlockTypeUtils.isBlockTypeBed(blockType)) {
                    addLocationToPlayerInitiatedBlockExplosions(playerId, location);
                }

                // If the block interacted with is a respawn anchor, and they are holding glowstone, store the player uuid and location where the explosion is expected.
                if(handItem != null) {
                    ItemType itemType = handItem.getType().asItemType();
                    if(itemType != null) {
                        if(BlockTypeUtils.isBlockTypeRespawnAnchor(blockType) && isRespawnAnchorChargesMax(block) && ItemTypeUtils.isItemTypeGlowstone(itemType)) {
                            addLocationToPlayerInitiatedBlockExplosions(playerId, location);
                        }
                    }
                }
            }
        }

        // If the block is TNT and they are holding a flint and steel or fire charge, s tore the uuid and location where the explosion is expected.
        if(handItem != null) {
            ItemType itemType = handItem.getType().asItemType();
            if(itemType != null) {
                if(blockType.equals(BlockType.TNT) && (ItemTypeUtils.isItemTypeFlintAndSteel(itemType) || ItemTypeUtils.isItemTypeFireCharge(itemType))) {
                    addLocationToPlayerInitiatedBlockExplosions(playerId, location);
                }
            }
        }
    }

    /**
     * Listens for when a player interacts with an entity that will initiate an {@link EntitySpawnEvent}.
     * @param playerInteractEntityEvent A {@link PlayerInteractEntityEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent playerInteractEntityEvent) {
        Player player = playerInteractEntityEvent.getPlayer();
        UUID playerId = player.getUniqueId();
        Entity entity = playerInteractEntityEvent.getRightClicked();
        UUID entityId = entity.getUniqueId();
        EntityType entityType = entity.getType();

        if(entityType.equals(EntityType.CREEPER)) {
            addEntityIdToPlayerInitiatedEntityExplosions(playerId, entityId);
        }
    }

    /**
     * Listens for when a {@link BlockExplodeEvent} occurs.
     * If the event is cancelled, the location will be removed from the {@link #playerInitiatedBlockExplosions} map.
     * Otherwise, the map will be iterated through checking if a player initiated the explosion by comparing the
     * location from the event to the locations stored for the player's UUID.
     * If a match is found, the {@link BlockExplodeEvent} is passed to the mine with the player.
     * If no match is found, the {@link BlockExplodeEvent} is passed to the mine without a player.
     * @param blockExplodeEvent A {@link BlockExplodeEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockExplode(BlockExplodeEvent blockExplodeEvent) {
        Location location = blockExplodeEvent.getBlock().getLocation();

        // If the event was cancelled, remove the location from all player's list of locations in the playerInitiatedBlockExplosions map and return.
        if(blockExplodeEvent.isCancelled()) {
            playerInitiatedBlockExplosions.keySet().forEach(playerId ->
                    removeLocationFromPlayerInitiatedBlockExplosions(playerId, location));

            return;
        }

        // Iterate through the playerInitiatedBlockExplosions map to check if the event location matches any locations attributed to a player.
        Iterator<Map.Entry<UUID, List<Location>>> playerDataIterator = playerInitiatedBlockExplosions.entrySet().iterator();
        while(playerDataIterator.hasNext()) {
            Map.Entry<UUID, List<Location>> entry = playerDataIterator.next();
            UUID playerId = entry.getKey();
            List<Location> locationList = entry.getValue();
            Iterator<Location> locationIterator = locationList.iterator();

            // Iterate through the locations stored for the player
            while(locationIterator.hasNext()) {
                Location listLocation = locationIterator.next();

                // If the locations match, remove the location from the list and call a PlayerExplodeBlockEvent
                if(listLocation.equals(location)) {
                    locationIterator.remove();

                    @Nullable Player player = skyMines.getServer().getPlayer(playerId);

                    AbstractMine mine = mineDataManager.getMineByLocation(location);
                    if(mine != null) {
                        mine.handleBlockExplodeEvent(player, blockExplodeEvent);
                    }

                    // Remove the location list for the player if empty
                    if(locationList.isEmpty()) {
                        playerDataIterator.remove();
                    }

                    return;
                }
            }
        }

        // If a player did not initiate the explosion, pass the BlockExplodeEvent to the mine without a player if applicable.
        AbstractMine mine = mineDataManager.getMineByLocation(location);
        if(mine != null) {
            mine.handleBlockExplodeEvent(null, blockExplodeEvent);
        }
    }

    /**
     * Listens for when a {@link EntityExplodeEvent} occurs.
     * If the event is cancelled, the entity id will be removed from the {@link #playerInitiatedEntityExplosions} map.
     * Otherwise, the map will be iterated through checking if a player initiated the explosion by comparing the
     * entity id from the event to the entity ids stored for player's UUIDs.
     * If a match is found, the {@link EntityExplodeEvent} is passed to the mine with the player.
     * If no match is found, the {@link EntityExplodeEvent} is passed to the mine without a player.
     * @param entityExplodeEvent An {@link EntityExplodeEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent entityExplodeEvent) {
        Entity entity = entityExplodeEvent.getEntity();
        UUID explodedEntityId = entity.getUniqueId();

        // If the event was cancelled, remove the entity id from all player's list of entity ids in the playerInitiatedEntityExplosions map and return.
        if(entityExplodeEvent.isCancelled()) {
            playerInitiatedEntityExplosions.keySet().forEach(playerId ->
                    removeEntityIdFromPlayerInitiatedEntityExplosions(playerId, explodedEntityId));

            return;
        }

        // Iterate through the playerInitiatedEntityExplosions map to check if the event entity id matches any entity ids attributed to a player.
        Iterator<Map.Entry<UUID, List<UUID>>> playerDataIterator = playerInitiatedEntityExplosions.entrySet().iterator();
        while(playerDataIterator.hasNext()) {
            Map.Entry<UUID, List<UUID>> entry = playerDataIterator.next();
            UUID playerId = entry.getKey();
            List<UUID> entityIdsList = entry.getValue();
            Iterator<UUID> entityIdIterator = entityIdsList.iterator();

            // Iterate through the entity ids stored for the player
            while(entityIdIterator.hasNext()) {
                UUID listEntityId = entityIdIterator.next();

                // If the list entity id list equals the exploded entity id, remove it from their list and fire a PlayerExplodeEntityEvent.
                if(listEntityId.equals(explodedEntityId)) {
                    entityIdIterator.remove();

                    @Nullable Player player = skyMines.getServer().getPlayer(playerId);

                    AbstractMine mine = mineDataManager.getMineByLocation(entityExplodeEvent.getLocation());
                    if(mine != null) {
                        mine.handleEntityExplodeEvent(player, entityExplodeEvent);
                    }

                    // Remove the entity id list for the player if empty
                    if(entityIdsList.isEmpty()) {
                        playerDataIterator.remove();
                    }

                    return;
                }
            }
        }

        // If a player did not initiate the explosion, pass the EntityExplodeEvent to the mine without a player if applicable.
        AbstractMine mine = mineDataManager.getMineByLocation(entityExplodeEvent.getLocation());
        if(mine != null) {
            mine.handleEntityExplodeEvent(null, entityExplodeEvent);
        }
    }

    /**
     * Listens for when an {@link EntitySpawnEvent} occurs for {@link TNTPrimed}.
     * If the event is cancelled, the location will be removed from the {@link #playerInitiatedBlockExplosions} map.
     * Otherwise, the map will be iterated through checking if a player initiated the spawning of the TNT by comparing
     * the spawn location to the list of locations stored for player's UUIDs.
     * If a match is found, the spawn location from the event wil be stored in {@link #playerInitiatedEntityExplosions}.
     * @param entitySpawnEvent An {@link EntitySpawnEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitySpawnEvent(EntitySpawnEvent entitySpawnEvent) {
        Entity entity = entitySpawnEvent.getEntity();

        // Only handle spawn entities for TNT
        if(entity instanceof TNTPrimed) {
            UUID entityId = entity.getUniqueId();
            Location spawnLocation = entitySpawnEvent.getLocation();

            // If the event was cancelled, remove the location from all player's list of locations in the playerInitiatedBlockExplosions map and return.
            if(entitySpawnEvent.isCancelled()) {
                playerInitiatedBlockExplosions.keySet().forEach(playerId ->
                        removeLocationFromPlayerInitiatedBlockExplosions(playerId, spawnLocation));

                return;
            }

            // Iterate through the playerInitiatedBlockExplosions map to check if the event location matches any the locations attributed to a player.
            Iterator<Map.Entry<UUID, List<Location>>> playerDataIterator = playerInitiatedBlockExplosions.entrySet().iterator();
            while(playerDataIterator.hasNext()) {
                Map.Entry<UUID, List<Location>> entry = playerDataIterator.next();
                UUID playerId = entry.getKey();
                List<Location> locationList = entry.getValue();
                Iterator<Location> locationIterator = locationList.iterator();

                // Iterate through the locations stored for the player
                while(locationIterator.hasNext()) {
                    Location listLocation = locationIterator.next();

                    // If the spawn location's block matches a block location store the entity id as player initiated.
                    if(listLocation.getWorld().getName().equals(spawnLocation.getWorld().getName())
                            && listLocation.getBlockX() == spawnLocation.getBlockX()
                            && listLocation.getBlockY() == spawnLocation.getBlockY()
                            && listLocation.getBlockZ() == spawnLocation.getBlockZ()) {
                        locationIterator.remove();

                        addEntityIdToPlayerInitiatedEntityExplosions(playerId, entityId);

                        // Remove the location list for the player if empty
                        if(locationList.isEmpty()) {
                            playerDataIterator.remove();
                        }

                        return;
                    }
                }
            }
        }
    }

    /**
     * Add a location to the {@link #playerInitiatedBlockExplosions} for a player.
     * @param playerId The player's {@link UUID}.
     * @param location The {@link Location}.
     */
    private void addLocationToPlayerInitiatedBlockExplosions(@NotNull UUID playerId, @NotNull Location location) {
        List<Location> locationList = playerInitiatedBlockExplosions.getOrDefault(playerId, new ArrayList<>());

        locationList.add(location);

        playerInitiatedBlockExplosions.put(playerId, locationList);
    }

    /**
     * Remove a location from the {@link #playerInitiatedBlockExplosions} for a player.
     * @param playerId The player's {@link UUID}.
     * @param location The {@link Location}.
     */
    private void removeLocationFromPlayerInitiatedBlockExplosions(@NotNull UUID playerId, @NotNull Location location) {
        if(!playerInitiatedBlockExplosions.containsKey(playerId)) return;

        List<Location> locationList = playerInitiatedBlockExplosions.get(playerId);

        locationList.remove(location);

        if(locationList.isEmpty()) {
            playerInitiatedBlockExplosions.remove(playerId);
            return;
        }

        playerInitiatedBlockExplosions.put(playerId, locationList);
    }

    /**
     * Add an entity {@link UUID} to the {@link #playerInitiatedEntityExplosions} for a player.
     * @param playerId The player's {@link UUID}.
     * @param entityId The entity's {@link UUID}.
     */
    private void addEntityIdToPlayerInitiatedEntityExplosions(@NotNull UUID playerId, @NotNull UUID entityId) {
        List<UUID> entityIdsList = playerInitiatedEntityExplosions.getOrDefault(playerId, new ArrayList<>());

        entityIdsList.add(entityId);

        playerInitiatedEntityExplosions.put(playerId, entityIdsList);
    }

    /**
     * Removes an entity {@link UUID} from the {@link #playerInitiatedEntityExplosions} for a player.
     * @param playerId The player's {@link UUID}.
     * @param entityId The entity's {@link UUID}.
     */
    private void removeEntityIdFromPlayerInitiatedEntityExplosions(@NotNull UUID playerId, @NotNull UUID entityId) {
        if(!playerInitiatedEntityExplosions.containsKey(playerId)) return;

        List<UUID> entityIdsList = playerInitiatedEntityExplosions.get(playerId);

        entityIdsList.remove(entityId);

        if(entityIdsList.isEmpty()) {
            playerInitiatedBlockExplosions.remove(playerId);
            return;
        }

        playerInitiatedEntityExplosions.put(playerId, entityIdsList);
    }

    /**
     * Check if a respawn anchor's charges are at the maximum.
     * @param block The {@link Block} for the respawn anchor.
     * @return true if at max charges, otherwise false. Will return false if not a respawn anchor.
     */
    private boolean isRespawnAnchorChargesMax(@NotNull Block block) {
        if(block.getBlockData() instanceof RespawnAnchor respawnAnchor) {
            return respawnAnchor.getCharges() == respawnAnchor.getMaximumCharges();
        }

        return false;
    }
}
