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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.world.WorldMineConfig;
import com.github.lukesky19.skymines.manager.bossbar.BossBarManager;
import com.github.lukesky19.skymines.manager.mine.world.BlocksManager;
import com.github.lukesky19.skymines.manager.mine.world.PDCManager;
import com.github.lukesky19.skymines.util.ItemTypeUtils;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.FlowerBed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This mine treats an entire world as a mine.
 * This mine is a shared mine between all players.
 * The blocks that can be broken and placed are configurable.
 * There is special handling for water-logged blocks and petal-based blocks that are placed by players.
 * Some actions like liquid flow are disabled.
 * TNT can be allowed only for unlocked or free blocks or disabled all together.
 */
public class WorldMine extends AbstractMine {
    // SkyMines
    private final @NotNull SkyMines skyMines;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull BlocksManager blocksManager;
    private final @NotNull BossBarManager bossBarManager;
    private final @NotNull PDCManager pdcManager;

    // Mine Data
    private boolean status = true;
    private final @NotNull WorldMineConfig mineConfig;
    private @Nullable String mineId;
    private @Nullable World mineWorld;

    private final @NotNull List<BlockType> unlockableBlockTypes = new ArrayList<>();
    private final @NotNull List<BlockType> freeBlockTypes = new ArrayList<>();
    private final @NotNull List<BlockType> restrictedPlaceableBlockTypes = new ArrayList<>();

    /**
     * Default Constructor.
     * You should use {@link #WorldMine(SkyMines, LocaleManager, BlocksManager, BossBarManager, PDCManager, WorldMineConfig)} instead.
     * @deprecated You should use {@link #WorldMine(SkyMines, LocaleManager, BlocksManager, BossBarManager, PDCManager, WorldMineConfig)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public WorldMine() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param blocksManager A {@link BlocksManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     * @param pdcManager A {@link PDCManager} instance.
     * @param mineConfig The {@link WorldMineConfig} to create the mine with.
     */
    public WorldMine(
            @NotNull SkyMines skyMines,
            @NotNull LocaleManager localeManager,
            @NotNull BlocksManager blocksManager,
            @NotNull BossBarManager bossBarManager, 
            @NotNull PDCManager pdcManager,
            @NotNull WorldMineConfig mineConfig) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.blocksManager = blocksManager;
        this.bossBarManager = bossBarManager;
        this.pdcManager = pdcManager;
        this.mineConfig = mineConfig;

        ComponentLogger logger = skyMines.getComponentLogger();

        if(mineConfig.mineId() != null) {
            this.mineId = mineConfig.mineId();
        } else {
            logger.error(AdventureUtil.serialize("Unable to create a world mine due to a null mine id."));
            status = false;
            return;
        }

        if(mineConfig.worldName() == null) {
            logger.error(AdventureUtil.serialize("<red>Unable to create a world mine due to a world name not being configured.</red>"));
            status = false;
            return;
        }

        World mineWorld = skyMines.getServer().getWorld(mineConfig.worldName());
        if(mineWorld == null) {
            logger.error(AdventureUtil.serialize("<red>Unable to create a world mine due to world " + mineConfig.worldName() + " not being found.</red>"));
            status = false;
            return;
        }
        this.mineWorld = mineWorld;

        mineConfig.unlockableBreakable().forEach(blockData -> {
            String blockTypeName = blockData.blockType();
            if(blockTypeName != null) {
                @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
                optionalBlockType.ifPresent(unlockableBlockTypes::add);
            }
        });

        mineConfig.freeBreakable().forEach(blockData -> {
            if(blockData.blockType() != null) {
                @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockData.blockType());
                optionalBlockType.ifPresent(freeBlockTypes::add);
            }
        });

        mineConfig.restrictedPlaceable().forEach(blockTypeName -> {
            @NotNull Optional<BlockType> optionalBlockType = RegistryUtil.getBlockType(logger, blockTypeName);
            optionalBlockType.ifPresent(restrictedPlaceableBlockTypes::add);
        });
    }

    /**
     * Get the id of the mine.
     * @return The id of the mine.
     */
    @Override
    public @Nullable String getMineId() {
        return mineId;
    }

    /**
     * Checks if the location's world and the mine's world are the same.
     * @param location The {@link Location} to check.
     * @return true if the location is inside the mine, otherwise false.
     */
    @Override
    public boolean isLocationInMine(@NotNull Location location) {
        if(mineWorld == null) return false;

        return location.getWorld().getName().equals(mineWorld.getName());
    }

    /**
     * Checks if the {@link Player} can mine the block type at the location.
     * If this mine is configured to allow breaking player placed blocks, and the block is player-placed, true will be returned.
     * If the block type is free to mine, then true will be returned.
     * If the block type is unlocked, then true will be returned.
     * @apiNote This method will return false even if the block has water-logged the block and doesn't account for petals.
     * Use {@link PDCManager#isBlockWaterLoggedByPlayer(Location)} and {@link PDCManager#getPetalCountPlacedByPlayer(Location)}
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} of the block.
     * @param blockType The {@link BlockType} of the block.
     * @return true if the block can be mined, otherwise false.
     */
    @Override
    public boolean isBlockMineable(@NotNull UUID uuid, @NotNull Location location, @NotNull BlockType blockType) {
        if(mineId == null) return false;

        // Check if the location is a player-placed block
        if(mineConfig.canBreakPlayerBlocks() != null && mineConfig.canBreakPlayerBlocks()) {
            return pdcManager.isBlockPlayerPlaced(location);
        }

        // Check if the block type is a free block
        if(isBlockTypeFree(blockType)) return true;

        // Check if the block is unlocked
        return isBlockTypeUnlocked(uuid, blockType);
    }

    /**
     * There are no cooldowns for world type mines. This will always return false.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} of the block.
     * @return Always false.
     */
    @Override
    public boolean isLocationOnCooldown(@NotNull UUID uuid, @NotNull Location location) {
        return false;
    }

    /**
     * Handles a {@link BlockBreakEvent}.
     * Creative players are ignored.
     * If the block is free, the block is allowed to be broken.
     * If the block is player-placed, the block is allowed to be broken.
     * If the block is player-placed and the block's UnlockBlockData is that of a a {@link FlowerBed}, there are
     * player-placed petals, and the player-placed petals are less than that of the flower bed's amount, the event
     * is cancelled and the proper amount of petals is updated and dropped.
     * If the block is unlockable and unlocked, the block is allowed to be broken.
     * Otherwise, the event is cancelled.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @Override
    public void handleBlockBreak(@NotNull BlockBreakEvent blockBreakEvent) {
        if(mineId == null) return;

        Locale locale = localeManager.getLocale();
        Player player = blockBreakEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        Block block = blockBreakEvent.getBlock();
        Location location = block.getLocation();
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;

        // Allow the block to be mined if the block type is configured to be free
        if(isBlockTypeFree(blockType)) {
            return;
        }

        // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be broken
        if(mineConfig.canBreakPlayerBlocks() != null && mineConfig.canBreakPlayerBlocks() && pdcManager.isBlockPlayerPlaced(location)) {
            if(block.getBlockData() instanceof FlowerBed flowerBed) {
                // Get the player-placed petal count
                int playerPlacedPetals = pdcManager.getPetalCountPlacedByPlayer(location);

                // Check if the block has player-placed petals
                if(playerPlacedPetals == 0) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
                    blockBreakEvent.setCancelled(true);
                    return;
                }

                if(playerPlacedPetals < flowerBed.getFlowerAmount()) {
                    blockBreakEvent.setCancelled(true);

                    Material material = flowerBed.getPlacementMaterial();
                    ItemType itemType = material.asItemType();
                    if(itemType == null) return;

                    int updatedAmount = flowerBed.getFlowerAmount() - playerPlacedPetals;
                    flowerBed.setFlowerAmount(updatedAmount);

                    ItemStack itemStack = itemType.createItemStack(playerPlacedPetals);
                    PlayerUtil.giveItem(player.getInventory(), itemStack, playerPlacedPetals, location);

                    // Update block data
                    block.setBlockData(flowerBed);
                }
            }

            return;
        }

        // Check if the block is unlockable, but not unlocked.
        if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotUnlocked()));
            blockBreakEvent.setCancelled(true);
            return;
        }

        // Check if the block is not unlockable and is not unlocked.
        if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            blockBreakEvent.setCancelled(true);
            return;
        }

        // Check if the block type is not unlockable and is unlocked.
        if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            blockBreakEvent.setCancelled(true);
        }
    }

    /**
     * Handles a {@link BlockDropItemEvent}.
     * Creative players are ignored.
     * If the {@link Location} was already processed by a {@link BlockBreakEvent}, the event is ignored.
     * If the block is free, the block is allowed to drop.
     * If the block is player-placed, the block is allowed to drop.
     * If the block is player-placed and the block's UnlockBlockData is that of a a {@link FlowerBed}, there are
     * player-placed petals, and the player-placed petals are less than that of the flower bed's amount, the event
     * is cancelled and the proper amount of petals is updated and dropped.
     * If the block is unlockable and unlocked, the block is allowed to drop.
     * Otherwise, the event is cancelled.
     * @param blockDropItemEvent A {@link BlockDropItemEvent}.
     */
    @Override
    public void handleBlockDropItem(@NotNull BlockDropItemEvent blockDropItemEvent) {
        Locale locale = localeManager.getLocale();
        Player player = blockDropItemEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        BlockState blockState = blockDropItemEvent.getBlockState();
        Location location = blockState.getLocation();
        Material material = blockState.getType();
        BlockType blockType = material.asBlockType();
        if(blockType == null) return;

        // Allow the block to be mined if the block type is configured to be free
        if(isBlockTypeFree(blockType)) {
            return;
        }

        // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be broken
        if(mineConfig.canBreakPlayerBlocks() != null && mineConfig.canBreakPlayerBlocks() && pdcManager.isBlockPlayerPlaced(location)) {
            if(blockState.getBlockData() instanceof FlowerBed flowerBed) {
                // Get the player-placed petal count
                int playerPlacedPetals = pdcManager.getPetalCountPlacedByPlayer(location);

                // Check if the block has player-placed petals
                if(playerPlacedPetals == 0) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
                    blockDropItemEvent.setCancelled(true);

                    // Revert the block state
                    BlockState currentBlockState = location.getBlock().getState(false);
                    currentBlockState.setType(material);
                    currentBlockState.setBlockData(blockState.getBlockData());
                    currentBlockState.update(true, false);

                    return;
                }

                if(playerPlacedPetals < flowerBed.getFlowerAmount()) {
                    blockDropItemEvent.setCancelled(true);

                    // Revert the block state
                    BlockState currentBlockState = location.getBlock().getState(false);
                    currentBlockState.setType(material);
                    currentBlockState.setBlockData(blockState.getBlockData());
                    currentBlockState.update(true, false);

                    Material placementMaterial = flowerBed.getPlacementMaterial();
                    ItemType itemType = placementMaterial.asItemType();
                    if(itemType == null) return;

                    int updatedAmount = flowerBed.getFlowerAmount() - playerPlacedPetals;
                    flowerBed.setFlowerAmount(updatedAmount);

                    ItemStack itemStack = itemType.createItemStack(playerPlacedPetals);
                    PlayerUtil.giveItem(player.getInventory(), itemStack, playerPlacedPetals, location);

                    // Set updated block data
                    currentBlockState.setBlockData(flowerBed);
                }
            }

            return;
        }

        // Check if the block is unlockable, but not unlocked.
        if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotUnlocked()));
            blockDropItemEvent.setCancelled(true);

            // Revert the block state
            BlockState currentBlockState = location.getBlock().getState(false);
            currentBlockState.setType(material);
            currentBlockState.setBlockData(blockState.getBlockData());
            currentBlockState.update(true, false);

            return;
        }

        // Check if the block is not unlockable and is not unlocked.
        if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            blockDropItemEvent.setCancelled(true);

            // Revert the block state
            BlockState currentBlockState = location.getBlock().getState(false);
            currentBlockState.setType(material);
            currentBlockState.setBlockData(blockState.getBlockData());
            currentBlockState.update(true, false);

            return;
        }

        // Check if the block type is not unlockable and is unlocked.
        if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            blockDropItemEvent.setCancelled(true);

            // Revert the block state
            BlockState currentBlockState = location.getBlock().getState(false);
            currentBlockState.setType(material);
            currentBlockState.setBlockData(blockState.getBlockData());
            currentBlockState.update(true, false);
        }
    }

    /**
     * Handles a {@link PlayerBucketFillEvent}.
     * Creative players are ignored.
     * If the block is free, the block is allowed to be scooped.
     * If the block is player-placed, the block is allowed to be scooped.
     * If the block is player-placed and the block's UnlockBlockData is that of a a {@link Waterlogged}, and the location is
     * player water-logged, the block is allowed to be scooped.
     * If the block is unlockable and unlocked, the block is allowed to be scooped.
     * Otherwise, the event is cancelled.
     * @param playerBucketFillEvent A {@link PlayerBucketFillEvent}.
     */
    @Override
    public void handleBucketFilled(@NotNull PlayerBucketFillEvent playerBucketFillEvent) {
        Locale locale = localeManager.getLocale();
        Player player = playerBucketFillEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        BlockState blockState = playerBucketFillEvent.getBlock().getState();
        Location location = blockState.getLocation();
        BlockType blockType = blockState.getType().asBlockType();
        if(blockType == null) return;

        // Allow the block to be mined if the block type is configured to be free
        if(isBlockTypeFree(blockType)) return;

        // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be broken
        if(mineConfig.canBreakPlayerBlocks() != null && mineConfig.canBreakPlayerBlocks()) {
            if(pdcManager.isBlockPlayerPlaced(location)) {
                return;
            }

            if(pdcManager.isBlockWaterLoggedByPlayer(location)) {
                return;
            }

            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            playerBucketFillEvent.setCancelled(true);
            return;
        }

        // Check if the block is unlockable, but not unlocked.
        if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotUnlocked()));
            playerBucketFillEvent.setCancelled(true);
            return;
        }

        // Check if the block is not unlockable and is not unlocked.
        if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            playerBucketFillEvent.setCancelled(true);
            return;
        }

        // Check if the block type is not unlockable and is unlocked.
        if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            playerBucketFillEvent.setCancelled(true);
            return;
        }

        pdcManager.markLocationAsPlayerPlaced(location);

        if(blockState.getBlockData() instanceof Waterlogged && blockType.equals(BlockType.WATER)) {
            pdcManager.markLocationAsPlayerWaterLogged(location);
        }
    }

    /**
     * Handles a {@link PlayerBucketEmptyEvent}.
     * Creative players are ignored.
     * If the mine is configured to not allow player-placed blocks, the event is cancelled.
     * If the block type is on the restricted placement list, the event is cancelled.
     * If block placement is restricted to unlocked and free block types and the block type is not unlocked or free, the event is cancelled.
     * Otherwise, the bucket's contents is placed and the location is marked as player-placed and water-logged if necessary.
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}.
     */
    @Override
    public void handleBucketEmptied(@NotNull PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        // Locale
        Locale locale = localeManager.getLocale();

        // Player
        Player player = playerBucketEmptyEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        // Block
        Block block = playerBucketEmptyEvent.getBlock();
        Location location = block.getLocation();

        // Bucket
        Material material = playerBucketEmptyEvent.getBucket();
        ItemType itemType = material.asItemType();
        if(itemType == null) return;

        // Block type
        BlockType blockType = ItemTypeUtils.mapBucketItemTypeToBlockType(itemType);
        if(blockType == null) return;

        // Check if the mine is configured to allow player placed blocks.
        if(mineConfig.canPlacePlayerBlocks() == null || !mineConfig.canPlacePlayerBlocks()) {
            playerBucketEmptyEvent.setCancelled(true);
            player.sendMessage(locale.prefix() + locale.worldMineMessages().blockPlaceNotAllowed());
            return;
        }

        // Check if the block type is a restricted block
        if(isBlockTypePlacementRestricted(blockType)) {
            playerBucketEmptyEvent.setCancelled(true);
            player.sendMessage(locale.prefix() + locale.worldMineMessages().blockPlaceNotAllowed());
            return;
        }

        // Check if player placed blocks are restricted to unlocked and free
        if(mineConfig.restrictPlaceToUnlockedAndFree() != null && mineConfig.restrictPlaceToUnlockedAndFree()) {
            if(!isBlockTypeFree(blockType)) {
                if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                    playerBucketEmptyEvent.setCancelled(true);
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockPlaceNotUnlocked()));
                    return;
                }

                if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                    playerBucketEmptyEvent.setCancelled(true);
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockPlaceNotAllowed()));
                    return;
                }
            }
        }

        if(block.getBlockData() instanceof Waterlogged) {
            pdcManager.markLocationAsPlayerWaterLogged(location);
        } else {
            pdcManager.markLocationAsPlayerPlaced(location);
        }
    }

    /**
     * Handles a {@link PlayerInteractEvent}.
     * Creative players are ignored.
     * Checks if the player has right-clicked a block with a hoe or shovel.
     * If the block is free, the interaction is allowed.
     * If the block is player-placed, the interaction is allowed.
     * If the block is unlockable and unlocked, the interaction is allowed.
     * Otherwise, the event is cancelled.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @Override
    public void handlePlayerInteract(@NotNull PlayerInteractEvent playerInteractEvent) {
        Locale locale = localeManager.getLocale();
        Player player = playerInteractEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        // Block
        Block block = playerInteractEvent.getClickedBlock();
        if(block == null) return;
        Location location = block.getLocation();
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;

        // Item
        ItemStack eventItemStack = playerInteractEvent.getItem();
        if(eventItemStack == null) return;
        ItemType itemType = eventItemStack.getType().asItemType();
        if(itemType == null) return;

        // If the ItemType is not a hoe, shovel, bone meal, shears, trial key, ominious trial key, or glass bottle, return.
        if(!ItemTypeUtils.isItemTypeHoe(itemType)
                && !ItemTypeUtils.isItemTypeShovel(itemType)
                && !ItemTypeUtils.isItemTypeBoneMeal(itemType)
                && !ItemTypeUtils.isItemTypeShears(itemType)
                && !ItemTypeUtils.isItemTypeKey(itemType)
                && !ItemTypeUtils.isItemTypeGlassBottle(itemType)) return;

        // Allow the block to be mined if the block type is configured to be free
        if(isBlockTypeFree(blockType)) return;

        // If the breaking of player-placed blocks is allowed and the locations is marked as player placed,
        // check if the block is allowed to be broken
        if(mineConfig.canBreakPlayerBlocks() != null
                && mineConfig.canBreakPlayerBlocks()
                && pdcManager.isBlockPlayerPlaced(location)) {
            return;
        }

        // Check if the block is unlockable, but not unlocked.
        if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockInteractionNotUnlocked()));
            playerInteractEvent.setCancelled(true);
            return;
        }

        // Check if the block is not unlockable and is not unlocked.
        if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockInteractionNotAllowed()));
            playerInteractEvent.setCancelled(true);
            return;
        }

        // Check if the block type is not unlockable and is unlocked.
        if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockInteractionNotAllowed()));
            playerInteractEvent.setCancelled(true);
        }
    }

    /**
     * Handles a {@link PlayerHarvestBlockEvent}.
     * Creative players are ignored.
     * If the block is free, the block is allowed to be harvested.
     * If the block is player-placed, the block is allowed to be harvested.
     * If the block is unlockable and unlocked, the block is allowed to be harvested.
     * Otherwise, the event is cancelled.
     * @param playerHarvestBlockEvent A {@link PlayerHarvestBlockEvent}.
     */
    @Override
    public void handlePlayerHarvestBlockEvent(@NotNull PlayerHarvestBlockEvent playerHarvestBlockEvent) {
        Locale locale = localeManager.getLocale();
        Player player = playerHarvestBlockEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        BlockState blockState = playerHarvestBlockEvent.getHarvestedBlock().getState();
        Location location = blockState.getLocation();
        BlockType blockType = blockState.getType().asBlockType();
        if(blockType == null) return;

        // Allow the block to be mined if the block type is configured to be free
        if(isBlockTypeFree(blockType)) {
            return;
        }

        // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be broken
        if(mineConfig.canBreakPlayerBlocks() != null
                && mineConfig.canBreakPlayerBlocks()
                && pdcManager.isBlockPlayerPlaced(location)) {
            return;
        }

        // Check if the block is unlockable, but not unlocked.
        if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotUnlocked()));
            playerHarvestBlockEvent.setCancelled(true);
            return;
        }

        // Check if the block is not unlockable and is not unlocked.
        if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            playerHarvestBlockEvent.setCancelled(true);
            return;
        }

        // Check if the block type is not unlockable and is unlocked.
        if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
            playerHarvestBlockEvent.setCancelled(true);
        }
    }

    /**
     * Handles a {@link BlockFertilizeEvent}.
     * If the event does not involve a player, the event is cancelled.
     * Creative players are ignored.
     * Blocks in this context is the for each block that is involved in the fertilization event.
     * If the block is free, the block is allowed to be fertilized.
     * If the block is player-placed, the block is allowed to be fertilized.
     * If the block is player-placed and the block's UnlockBlockData is that of a {@link FlowerBed} and there are
     * player-placed petals, the block is allowed to be fertilized.
     * If the block is unlockable and unlocked, the block is allowed to be fertilized.
     * Otherwise, the event is cancelled.
     * @implNote It is know that if one flower is added to a natural {@link FlowerBed} by a player, it can be fertilized like it was entirely player-placed. There is no solution to this.
     * @param blockFertilizeEvent A {@link BlockFertilizeEvent}
     */
    @Override
    public void handleBlockFertilizeEvent(@NotNull BlockFertilizeEvent blockFertilizeEvent) {
        Player player = blockFertilizeEvent.getPlayer();
        if(player == null) {
            blockFertilizeEvent.setCancelled(true);
            return;
        }

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        UUID uuid = player.getUniqueId();
        List<BlockState> blockStateList = blockFertilizeEvent.getBlocks();

        Iterator<BlockState> iterator = blockStateList.iterator();
        while(iterator.hasNext()) {
            BlockState blockState = iterator.next();
            Location location = blockState.getLocation();
            BlockType blockType = blockState.getType().asBlockType();
            if(blockType == null) continue;

            // Allow the block to be fertilized if the block type is configured to be free
            if(isBlockTypeFree(blockType)) continue;

            // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be fertilized
            if(mineConfig.canBreakPlayerBlocks() != null
                    && mineConfig.canBreakPlayerBlocks()
                    && pdcManager.isBlockPlayerPlaced(location)) {
                if(blockState.getBlockData() instanceof FlowerBed) {
                    // Get the player-placed petal count
                    int playerPlacedPetals = pdcManager.getPetalCountPlacedByPlayer(location);

                    // Check if the block has player-placed petals
                    if(playerPlacedPetals == 0) {
                        iterator.remove();
                        continue;
                    }
                }

                continue;
            }

            // Check if the block is unlockable, but not unlocked.
            if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block is not unlockable and is not unlocked.
            if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block type is not unlockable and is unlocked.
            if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
            }
        }

        if(blockStateList.isEmpty()) {
            blockFertilizeEvent.setCancelled(true);
        }
    }

    /**
     * Handles a {@link StructureGrowEvent}.
     * Creative players are ignored.
     * If the block can not be mined according to {@link #isBlockMineable(UUID, Location, BlockType)}, then the block is removed from the list of blocks fertilized.
     *
     * @param structureGrowEvent A {@link StructureGrowEvent}.
     */
    @Override
    public void handleStructureGrowEvent(@NotNull StructureGrowEvent structureGrowEvent) {
        Player player = structureGrowEvent.getPlayer();
        if(player == null) return;

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        UUID uuid = player.getUniqueId();
        List<BlockState> blockStateList = structureGrowEvent.getBlocks();

        Iterator<BlockState> iterator = blockStateList.iterator();
        while(iterator.hasNext()) {
            BlockState blockState = iterator.next();
            Location location = blockState.getLocation();
            BlockType blockType = blockState.getType().asBlockType();
            if(blockType == null) continue;

            // Allow the block to be fertilized if the block type is configured to be free
            if(isBlockTypeFree(blockType)) continue;

            // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be fertilized
            if(mineConfig.canBreakPlayerBlocks() != null
                    && mineConfig.canBreakPlayerBlocks()
                    && pdcManager.isBlockPlayerPlaced(location)) {
                if(blockState.getBlockData() instanceof FlowerBed) {
                    // Get the player-placed petal count
                    int playerPlacedPetals = pdcManager.getPetalCountPlacedByPlayer(location);

                    // Check if the block has player-placed petals
                    if(playerPlacedPetals == 0) {
                        iterator.remove();
                        continue;
                    }
                }

                continue;
            }

            // Check if the block is unlockable, but not unlocked.
            if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block is not unlockable and is not unlocked.
            if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block type is not unlockable and is unlocked.
            if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
            }
        }

        if(blockStateList.isEmpty()) {
            structureGrowEvent.setCancelled(true);
        }
    }

    /**
     * Handles a {@link EntityChangeBlockEvent}.
     * Non-players are ignored.
     * Creative players are ignored.
     * If the block is free, the block is allowed to be changed.
     * If the block is player-placed, the block is allowed to be changed.
     * If the block is player-placed and the block's UnlockBlockData is that of a {@link FlowerBed} and there are
     * player-placed petals, the block is allowed to be changed.
     * If the block is unlockable and unlocked, the block is allowed to be broken.
     * Otherwise, the event is cancelled.
     * @implNote It is know that if one flower is added to a natural {@link FlowerBed} by a player, it can be changed like it was entirely player-placed. There is no solution to this.
     * @param entityChangeBlockEvent A {@link EntityChangeBlockEvent}.
     */
    @Override
    public void handleEntityChangeBlockEvent(@NotNull EntityChangeBlockEvent entityChangeBlockEvent) {
        Locale locale = localeManager.getLocale();
        Entity entity = entityChangeBlockEvent.getEntity();
        if(entity instanceof Player player) {
            if(player.getGameMode().equals(GameMode.CREATIVE)) return;

            UUID uuid = player.getUniqueId();
            Block block = entityChangeBlockEvent.getBlock();
            Location location = block.getLocation();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) {
                return;
            }

            // Allow the block to be mined if the block type is configured to be free
            if(isBlockTypeFree(blockType)) {
                return;
            }

            // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be broken
            if(mineConfig.canBreakPlayerBlocks() != null
                    && mineConfig.canBreakPlayerBlocks()
                    && pdcManager.isBlockPlayerPlaced(location)) {
                return;
            }

            // Check if the block is unlockable, but not unlocked.
            if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotUnlocked()));
                entityChangeBlockEvent.setCancelled(true);
                return;
            }

            // Check if the block is not unlockable and is not unlocked.
            if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
                entityChangeBlockEvent.setCancelled(true);
                return;
            }

            // Check if the block type is not unlockable and is unlocked.
            if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockBreakNotAllowed()));
                entityChangeBlockEvent.setCancelled(true);
            }
        }
    }

    /**
     * Handles a {@link BlockExplodeEvent}
     * If the event does not involve a player, the event is cancelled.
     * Creative players are ignored.
     * If player-initiated explosions are not allowed, the event is always cancelled.
     * Blocks in this context is the for each block that is involved in the explosion event.
     * If the block is free, the block is allowed to be destroyed.
     * If the block is player-placed, the block is allowed to be destroyed.
     * If the block is player-placed and the block's UnlockBlockData is that of a {@link FlowerBed} and there are
     * player-placed petals, the block is allowed to be destroyed for the amount that was placed.
     * If the block is unlockable and unlocked, the block is allowed to be destroyed.
     * Otherwise, the rest of the blocks are not allowed to explode. The event is cancelled if no blocks were exploded.
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param blockExplodeEvent A {@link BlockExplodeEvent}
     */
    @Override
    public void handleBlockExplodeEvent(@Nullable Player player, @NotNull BlockExplodeEvent blockExplodeEvent) {
        // If the explosion is not player initiated, cancel the event.
        // The explosion is treated as not player initiated if they not online or are not connected.
        if(player == null || !player.isOnline() || !player.isConnected()) {
            blockExplodeEvent.setCancelled(true);
            return;
        }

        // Ignore creative players.
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        // If player initiated explosions are not allowed, cancel the event.
        if(mineConfig.allowPlayerExplosions() == null || !mineConfig.allowPlayerExplosions()) {
            blockExplodeEvent.setCancelled(true);
            return;
        }

        UUID uuid = player.getUniqueId();
        List<Block> explodedBlocks = blockExplodeEvent.blockList();

        Iterator<Block> iterator = explodedBlocks.iterator();
        while(iterator.hasNext()) {
            Block block = iterator.next();
            Location location = block.getLocation();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) continue;

            // Allow the block to be fertilized if the block type is configured to be free
            if(isBlockTypeFree(blockType)) continue;

            // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be fertilized
            if(mineConfig.canBreakPlayerBlocks() != null
                    && mineConfig.canBreakPlayerBlocks()
                    && pdcManager.isBlockPlayerPlaced(location)) {
                if(block.getBlockData() instanceof FlowerBed) {
                    // Get the player-placed petal count
                    int playerPlacedPetals = pdcManager.getPetalCountPlacedByPlayer(location);

                    // Check if the block has player-placed petals
                    if(playerPlacedPetals == 0) {
                        iterator.remove();
                        continue;
                    }
                }

                continue;
            }

            // Check if the block is unlockable, but not unlocked.
            if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block is not unlockable and is not unlocked.
            if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block type is not unlockable and is unlocked.
            if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
            }
        }

        if(explodedBlocks.isEmpty()) {
            blockExplodeEvent.setCancelled(true);
        }
    }

    /**
     * Handles an {@link EntityExplodeEvent}
     * If the event does not involve a player, the event is cancelled.
     * If player-initiated explosions are not allowed, the event is always cancelled.
     * Creative players are ignored.
     * Blocks in this context is the for each block that is involved in the explosion event.
     * If the block is free, the block is allowed to be destroyed.
     * If the block is player-placed, the block is allowed to be destroyed.
     * If the block is player-placed and the block's UnlockBlockData is that of a {@link FlowerBed} and there are
     * player-placed petals, the block is allowed to be destroyed for the amount that was placed.
     * If the block is unlockable and unlocked, the block is allowed to be destroyed.
     * Otherwise, the rest of the blocks are not allowed to explode. The event is cancelled if no blocks were exploded.
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param entityExplodeEvent A {@link EntityExplodeEvent}
     */
    @Override
    public void handleEntityExplodeEvent(@Nullable Player player, @NotNull EntityExplodeEvent entityExplodeEvent) {
        // If the explosion is not player initiated, cancel the event.
        // The explosion is treated as not player initiated if they not online or are not connected.
        if(player == null || !player.isOnline() || !player.isConnected()) {
            entityExplodeEvent.setCancelled(true);
            return;
        }

        // Ignore creative players.
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        // If player initiated explosions are not allowed, cancel the event.
        if(mineConfig.allowPlayerExplosions() == null || !mineConfig.allowPlayerExplosions()) {
            entityExplodeEvent.setCancelled(true);
            return;
        }

        UUID uuid = player.getUniqueId();
        List<Block> explodedBlocks = entityExplodeEvent.blockList();

        Iterator<Block> iterator = explodedBlocks.iterator();
        while(iterator.hasNext()) {
            Block block = iterator.next();
            Location location = block.getLocation();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) continue;

            // Allow the block to be fertilized if the block type is configured to be free
            if(isBlockTypeFree(blockType)) continue;

            // If the breaking of player-placed blocks is allowed and the locations is marked as player placed, check if the block is allowed to be fertilized
            if(mineConfig.canBreakPlayerBlocks() != null
                    && mineConfig.canBreakPlayerBlocks()
                    && pdcManager.isBlockPlayerPlaced(location)) {
                if(block.getBlockData() instanceof FlowerBed) {
                    // Get the player-placed petal count
                    int playerPlacedPetals = pdcManager.getPetalCountPlacedByPlayer(location);

                    // Check if the block has player-placed petals
                    if(playerPlacedPetals == 0) {
                        iterator.remove();
                        continue;
                    }
                }

                continue;
            }

            // Check if the block is unlockable, but not unlocked.
            if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block is not unlockable and is not unlocked.
            if(!unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
                continue;
            }

            // Check if the block type is not unlockable and is unlocked.
            if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                iterator.remove();
            }
        }

        if(explodedBlocks.isEmpty()) {
            entityExplodeEvent.setCancelled(true);
        }
    }

    /**
     * Handles a {@link BlockFromToEvent}.
     * This event is always cancelled. Prevents water and lava from flowing.
     * @param blockFromToEvent A {@link BlockFromToEvent}
     */
    @Override
    public void handleBlockFromToEvent(@NotNull BlockFromToEvent blockFromToEvent) {
        blockFromToEvent.setCancelled(true);
    }

    /**
     * Handles a {@link BlockPlaceEvent}.
     * Creative players are ignored.
     * If the mine doesn't allow player-placed blocks, cancel the event.
     * If block placement is restricted and the block type matches, cancel the event.
     * If block placement is restricted to unlocked and free, and the block type is not unlocked or free, cancel the event.
     * Otherwise, the location is marked as player-placed, player water-logged if necessary, and how many petals were placed if necessary.
     * @param blockPlaceEvent A {@link BlockPlaceEvent}
     */
    @Override
    public void handleBlockPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
        Locale locale = localeManager.getLocale();
        Player player = blockPlaceEvent.getPlayer();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        UUID uuid = player.getUniqueId();
        Block block = blockPlaceEvent.getBlock();
        Location location = block.getLocation();
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;

        // Check if the mine is configured to allow player placed blocks.
        if(mineConfig.canPlacePlayerBlocks() == null || !mineConfig.canPlacePlayerBlocks()) {
            blockPlaceEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockPlaceNotAllowed()));
            return;
        }

        // Check if the block type is a restricted block
        if(isBlockTypePlacementRestricted(blockType)) {
            blockPlaceEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockPlaceNotAllowed()));
            return;
        }

        // Check if player placed blocks are restricted to unlocked and free
        if(mineConfig.restrictPlaceToUnlockedAndFree() != null && mineConfig.restrictPlaceToUnlockedAndFree()) {
            if(!isBlockTypeFree(blockType)) {
                if(unlockableBlockTypes.contains(blockType) && !isBlockTypeUnlocked(uuid, blockType)) {
                    blockPlaceEvent.setCancelled(true);
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockPlaceNotUnlocked()));
                    return;
                }

                if(!unlockableBlockTypes.contains(blockType) && isBlockTypeUnlocked(uuid, blockType)) {
                    blockPlaceEvent.setCancelled(true);
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.worldMineMessages().blockPlaceNotAllowed()));
                    return;
                }
            }
        }

        // Mark the location as player-placed.
        pdcManager.markLocationAsPlayerPlaced(location);

        // Run some data stored 1 tick later after the block was placed.
        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
            // If the block is water-loggable and water-logged, mark the location as player water-logged.
            if(block.getBlockData() instanceof Waterlogged waterlogged) {
                if(waterlogged.isWaterlogged()) {
                    pdcManager.markLocationAsPlayerWaterLogged(location);
                }
            }

            // If the block is a flower bed, add a petal count placed by a player
            if(block.getBlockData() instanceof FlowerBed) {
                pdcManager.addPetalCountPlacedByPlayer(location);
            }
        }, 1L);
    }

    /**
     * Handles a {@link PlayerMoveEvent}.
     * Hides or shows the boss bar depending on if they are exiting this mine or entering this mine.
     * If they don't leave or enter a mine, nothing happens.
     * @param playerMoveEvent A {@link PlayerMoveEvent}.
     */
    @Override
    public void handlePlayerMoveEvent(@NotNull PlayerMoveEvent playerMoveEvent) {
        Location from = playerMoveEvent.getFrom();
        Location to = playerMoveEvent.getTo();

        Player player = playerMoveEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(isLocationInMine(from) && !isLocationInMine(to)) {
            bossBarManager.removeBossBar(player, uuid);
        } else if(!isLocationInMine(from) && isLocationInMine(to)) {
            createAndShowBossBar(player, uuid);
        }
    }

    /**
     * Handles a {@link PlayerTeleportEvent}.
     * Hides or shows the boss bar depending on if they are exiting this mine or entering this mine.
     * If they don't leave or enter a mine, nothing happens.
     * @param playerTeleportEvent A {@link PlayerTeleportEvent}.
     */
    @Override
    public void handlePlayerTeleportEvent(@NotNull PlayerTeleportEvent playerTeleportEvent) {
        Location from = playerTeleportEvent.getFrom();
        Location to = playerTeleportEvent.getTo();

        Player player = playerTeleportEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(isLocationInMine(from) && !isLocationInMine(to)) {
            bossBarManager.removeBossBar(player, uuid);
        } else if(!isLocationInMine(from) && isLocationInMine(to)) {
            createAndShowBossBar(player, uuid);
        }
    }

    /**
     * Handles a {@link PlayerChunkLoadEvent}.
     * This method does nothing for this mine.
     * @param playerChunkLoadEvent A {@link PlayerChunkLoadEvent}.
     */
    @Override
    public void handlePlayerChunkLoad(@NotNull PlayerChunkLoadEvent playerChunkLoadEvent) {}

    /**
     * Create and show the boss bar for this mine to the player.
     * @param player The {@link Player} to show the boss bar.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void createAndShowBossBar(@NotNull Player player, @NotNull UUID uuid) {
        if(mineId == null) return;

        BossBar.Color bossBarColor;
        BossBar.Overlay bossBarOverlay;
        try {
            bossBarColor = BossBar.Color.valueOf(mineConfig.bossBar().color());
            bossBarOverlay = BossBar.Overlay.valueOf(mineConfig.bossBar().overlay());
        } catch (IllegalArgumentException e) {
            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to show boss bar due to a configuration error. " + e.getMessage()));
            return;
        }

        if(mineConfig.bossBar().text() == null) {
            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to create boss bar due no text configured."));
            return;
        }

        BossBar bossBar = BossBar.bossBar(AdventureUtil.serialize(mineConfig.bossBar().text()), 1, bossBarColor, bossBarOverlay);

        bossBarManager.setBossBar(player, uuid, bossBar);
    }

    /**
     * Update the boss bar shown to the player.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void updateBossBar(@NotNull UUID uuid) {
        if(mineId == null) return;

        BossBar bossBar = bossBarManager.getBossBar(uuid);
        if(bossBar == null) return;

        if(mineConfig.bossBar().text() == null) {
            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to update boss bar due no text configured."));
            return;
        }

        bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().text()));
    }

    /**
     * Cleans up any data for this mine on unload.
     * Will remove any boss bars from players.
     * @param onDisable Is the plugin being disabled?
     */
    @Override
    public void cleanUp(boolean onDisable) {
        for(Player player : skyMines.getServer().getOnlinePlayers()) {
            if(player.isOnline() && player.isConnected()) {
                UUID uuid = player.getUniqueId();
                Location playerLocation = player.getLocation();

                if(isLocationInMine(playerLocation)) {
                    bossBarManager.removeBossBar(player, uuid);
                }
            }
        }
    }

    /**
     * Was the mine created and setup successfully?
     * @return true if successful, or false.
     */
    @Override
    public boolean isSetup() {
        return status;
    }

    /**
     * Has the player unlocked the {@link BlockType} provided?
     * @param uuid The {@link UUID} of the player.
     * @param blockType The {@link BlockType} to check.
     * @return true if unlocked, otherwise false. Will always return false if {@link #mineId} is null.
     */
    private boolean isBlockTypeUnlocked(@NotNull UUID uuid, @NotNull BlockType blockType) {
        if(mineId == null) return false;

        return blocksManager.isBlockTypeUnlocked(uuid, mineId, blockType);
    }

    /**
     * Is the {@link BlockType} provided free to mine?
     * @param blockType The {@link BlockType} to check.
     * @return true if free to mine, otherwise false.
     */
    private boolean isBlockTypeFree(@NotNull BlockType blockType) {
        return freeBlockTypes.contains(blockType);
    }

    /**
     * Is the {@link BlockType} provided not allowed to be placed?
     * @param blockType The {@link BlockType} to check.
     * @return true if placement is allowed, otherwise false.
     */
    private boolean isBlockTypePlacementRestricted(@NotNull BlockType blockType) {
        if(!restrictedPlaceableBlockTypes.isEmpty()) {
            return restrictedPlaceableBlockTypes.contains(blockType);
        }

        return false;
    }
}
