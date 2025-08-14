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
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.packet.PacketMineConfig;
import com.github.lukesky19.skymines.data.packet.BlockData;
import com.github.lukesky19.skymines.data.packet.PacketBlock;
import com.github.lukesky19.skymines.manager.bossbar.BossBarManager;
import com.github.lukesky19.skymines.manager.config.LocaleManager;
import com.github.lukesky19.skymines.manager.mine.packet.CooldownManager;
import com.github.lukesky19.skymines.manager.mine.packet.MineTimeManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This mine uses packets to send block updates after the block has been mined.
 * Blocks that have been mined are still tracked server-side.
 * This allows the existence of a per-player mine system.
 */
@SuppressWarnings("deprecation")
public class PacketMine extends AbstractMine {
    // SkyMines
    private final @NotNull SkyMines skyMines;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull CooldownManager cooldownManager;
    private final @NotNull MineTimeManager mineTimeManager;
    private final @NotNull BossBarManager bossBarManager;

    // WorldGuard
    private @Nullable RegionManager regionManager;

    // Mine Data
    private boolean status = true;
    private final @NotNull PacketMineConfig mineConfig;
    private @Nullable String mineId;
    private @Nullable World mineWorld;
    private @Nullable ProtectedRegion mineRegion;
    /**
     * Contains the data for a {@link ProtectedRegion} and the {@link List} of {@link PacketBlock}s that contains the data to identify if a block can be mined and the data required to replace the block.
     */
    private final @NotNull Map<ProtectedRegion, List<PacketBlock>> blockDataByRegion = new HashMap<>();

    /**
     * Default Constructor.
     * You should use {@link #PacketMine(SkyMines, LocaleManager, CooldownManager, MineTimeManager, BossBarManager, PacketMineConfig)} instead.
     * @deprecated You should use {@link #PacketMine(SkyMines, LocaleManager, CooldownManager, MineTimeManager, BossBarManager, PacketMineConfig)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated
    public PacketMine() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMines A {@link SkyMines} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     * @param mineTimeManager A {@link MineTimeManager} instance.
     * @param bossBarManager A {@link BossBarManager} instance.
     * @param mineConfig The {@link PacketMineConfig} to create the mine with.
     */
    public PacketMine(
            @NotNull SkyMines skyMines,
            @NotNull LocaleManager localeManager,
            @NotNull CooldownManager cooldownManager,
            @NotNull MineTimeManager mineTimeManager,
            @NotNull BossBarManager bossBarManager,
            @NotNull PacketMineConfig mineConfig) {
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.cooldownManager = cooldownManager;
        this.mineTimeManager = mineTimeManager;
        this.bossBarManager = bossBarManager;
        this.mineConfig = mineConfig;

        ComponentLogger logger = skyMines.getComponentLogger();

        if(mineConfig.mineId() != null) {
            this.mineId = mineConfig.mineId();
        } else {
            logger.error(AdventureUtil.serialize("Unable to create mine due to a null mine id."));
            status = false;
            return;
        }

        if(mineConfig.worldName() == null) {
            logger.error(AdventureUtil.serialize("<red>Unable to create mine due to a world name not being configured.</red>"));
            status = false;
            return;
        }

        World mineWorld = skyMines.getServer().getWorld(mineConfig.worldName());
        if(mineWorld == null) {
            logger.error(AdventureUtil.serialize("<red>Unable to create mine due to world " + mineConfig.worldName() + " not being found.</red>"));
            status = false;
            return;
        }
        this.mineWorld = mineWorld;

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(mineWorld));
        if(regionManager == null) {
            logger.error(AdventureUtil.serialize("<red>Unable to create mine due to an invalid region manager. Is the world of name " + mineConfig.worldName() + " valid?</red>"));
            status = false;
            return;
        }
        this.regionManager = regionManager;

        ProtectedRegion parentRegion = getRegion(mineConfig.parentRegion());
        if(parentRegion == null) {
            logger.error(AdventureUtil.serialize("Unable to create mine due to parent region " + mineConfig.parentRegion() + " not being found."));
            status = false;
            return;
        }
        this.mineRegion = parentRegion;

        for(PacketMineConfig.ChildRegionData childRegionData :  mineConfig.childRegions()) {
            ProtectedRegion childRegion = getRegion(childRegionData.region());
            if(childRegion == null) {
                logger.warn(AdventureUtil.serialize("Unable to find a child region for " + childRegionData.region() + "."));
                continue;
            }

            List<PacketBlock> packetBlockList = new ArrayList<>();
            for(int i = 0; i < childRegionData.blocksAllowed().size(); i++) {
                PacketMineConfig.BlockData blockData = childRegionData.blocksAllowed().get(i);

                if(blockData.block() == null || blockData.replacement() == null) {
                    logger.warn(AdventureUtil.serialize("Invalid block data config at index " + i + ". This is because of a world type or replacement type is not configured for mine " + mineId + "."));
                    continue;
                }

                @NotNull Optional<BlockType> optionalBlockWorldType = RegistryUtil.getBlockType(logger, blockData.block());
                @NotNull Optional<BlockType> optionalBlockReplacementType = RegistryUtil.getBlockType(logger, blockData.replacement());

                if(optionalBlockWorldType.isEmpty() || optionalBlockReplacementType.isEmpty()) {
                    logger.warn(AdventureUtil.serialize("Invalid block data config at index " + i + ". This is because of an invalid world or replacement BlockType for mine " + mineId + "."));
                    continue;
                }

                BlockType worldBlockType = optionalBlockWorldType.get();
                BlockType replacementBlockType = optionalBlockReplacementType.get();
                Material worldMaterial = worldBlockType.asMaterial();
                Material replacementMaterial = replacementBlockType.asMaterial();
                if(worldMaterial == null || replacementMaterial == null) {
                    logger.warn(AdventureUtil.serialize("Invalid block data config at index " + i + ". This is because of an invalid world or replacement BlockType for mine " + mineId + "."));
                    continue;
                }

                LootTable lootTable = null;
                if(blockData.lootTable() != null) {
                    NamespacedKey key = NamespacedKey.fromString(blockData.lootTable());
                    if(key != null) {
                        lootTable = skyMines.getServer().getLootTable(key);
                    } else {
                        logger.warn(AdventureUtil.serialize("Unable to get loot table due to a null NamespacedKey at index " + i + " for mine " + mineId + "."));
                    }
                }

                PacketBlock packetBlock = new PacketBlock(worldBlockType, replacementBlockType, lootTable, blockData.cooldownSeconds());
                packetBlockList.add(packetBlock);
            }

            blockDataByRegion.put(childRegion, packetBlockList);
        }
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
     * Checks if the location is inside the mine's region. This only checks the parent region and not any child regions.
     * You can use {@link #isBlockMineable(UUID, Location, BlockType)} for checking child regions.
     * @param location The {@link Location} to check.
     * @return true if the location is inside the mine's parent region, otherwise false.
     */
    @Override
    public boolean isLocationInMine(@NotNull Location location) {
        if(mineWorld == null) return false;
        if(mineRegion == null) return false;

        return mineWorld.equals(location.getWorld())
                && mineRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Checks if the {@link Player} has access to the mine and if the block is allowed to be mined.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} of the block.
     * @param blockType The {@link BlockType} of the block.
     * @return true if the block can be mined, otherwise false.
     */
    @Override
    public boolean isBlockMineable(@NotNull UUID uuid, @NotNull Location location, @NotNull BlockType blockType) {
        if(mineId == null) return false;
        if(!mineTimeManager.hasMineTime(uuid, mineId)) return false;

        return blockDataByRegion.entrySet().stream()
                .filter(entry -> entry.getKey().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                .flatMap(entry -> entry.getValue().stream())
                .anyMatch(packetBlock -> packetBlock.worldType().equals(blockType));
    }

    /**
     * Checks if the {@link Location} is on cooldown for the player.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} of the block.
     * @return true if on cooldown, otherwise false.
     */
    @Override
    public boolean isLocationOnCooldown(@NotNull UUID uuid, @NotNull Location location) {
        return cooldownManager.isLocationOnCooldown(uuid, location);
    }

    /**
     * Handles a {@link BlockBreakEvent} that occurs while in the mine.
     * Creative players are ignored.
     * If the player has no time for the mine, a no access message is sent to the player.
     * If the block's location is on cooldown, a cooldown message is sent to the player.
     * If the block is not minable at all, a can not mine message is sent to the player.
     * The {@link BlockBreakEvent} is cancelled in the above 3 scenarios.
     * @param blockBreakEvent A BlockBreakEvent
     */
    @Override
    public void handleBlockBreak(@NotNull BlockBreakEvent blockBreakEvent) {
        if(mineId == null) return;
        Player player = blockBreakEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!mineTimeManager.hasMineTime(uuid, mineId)) {
            blockBreakEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineAccessNoTime()));
            return;
        }

        Block block = blockBreakEvent.getBlock();
        Location location = block.getLocation();
        Material blockMaterial = block.getType();
        BlockType blockType = blockMaterial.asBlockType();
        if(blockType == null) return;

        if(isLocationOnCooldown(uuid, location)) {
            blockBreakEvent.setCancelled(true);
            sendBulkBlockUpdates(player, uuid);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().cooldown()));
        } else {
            if(!isBlockMineable(uuid, location, blockType)) {
                blockBreakEvent.setCancelled(true);
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().canNotBreakBlock()));
            }
        }
    }

    /**
     * Checks if the player has time to access the mine and if the block can be mined.
     * If the block can be mined, the block is replaced, the items given to the player, and sends client-side block updates.
     * Players in creative mode will be ignored.
     * @param blockDropItemEvent A BlockDropItemEvent
     */
    @Override
    public void handleBlockDropItem(@NotNull BlockDropItemEvent blockDropItemEvent) {
        if(mineId == null) return;

        Player player = blockDropItemEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!mineTimeManager.hasMineTime(uuid, mineId)) {
            blockDropItemEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineAccessNoTime()));
            return;
        }

        BlockState blockState = blockDropItemEvent.getBlockState();
        Location location = blockState.getLocation();
        Material brokenMaterial = blockState.getType();
        BlockType brokenBlockType = brokenMaterial.asBlockType();
        if(brokenBlockType == null) return;

        blockDropItemEvent.setCancelled(true);

        // Replace the broken block
        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
            BlockState currentBlockState = location.getBlock().getState(false);
            currentBlockState.setType(brokenMaterial);
            currentBlockState.setBlockData(blockState.getBlockData());
            currentBlockState.update(true, false);
        }, 1L);

        blockDataByRegion.entrySet().stream()
                .filter(entry -> entry.getKey().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                .map(entry -> entry.getValue().stream()
                        .filter(packetBlock -> brokenBlockType.equals(packetBlock.worldType()))
                        .findFirst()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(packetBlock -> {
                    // Add dropped items to the player's inventory.
                    for(Item droppedItem : blockDropItemEvent.getItems()) {
                        ItemStack itemStack = droppedItem.getItemStack();
                        PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                    }

                    BlockType replacementBlockType = packetBlock.replacementType();
                    Material replacementMaterial = replacementBlockType.asMaterial();

                    cooldownManager.addLocationCooldown(uuid, location, packetBlock.replacementType(), packetBlock.cooldownSeconds());

                    if(replacementMaterial != null) {
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                            if (player.isOnline() && player.isConnected()) {
                                player.sendBlockChange(location, replacementMaterial.createBlockData());
                            }
                        }, 2L);
                    }
                });
    }

    /**
     * Handles a {@link PlayerBucketFillEvent} that occurs while in the mine.
     * Creative players are ignored.
     * If the player has no time for the mine, a no access message is sent to the player.
     * If the block's location is on cooldown, a cooldown message is sent to the player.
     * If the block is not minable at all, a can not mine message is sent to the player.
     * Otherwise, the appropriate bucket will be given to the player and the block state will be reverted.
     * The event is always cancelled.
     * @param playerBucketFillEvent A {@link PlayerBucketFillEvent}
     */
    @Override
    public void handleBucketFilled(@NotNull PlayerBucketFillEvent playerBucketFillEvent) {
        if(mineId == null) return;
        Player player = playerBucketFillEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!mineTimeManager.hasMineTime(uuid, mineId)) {
            playerBucketFillEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineAccessNoTime()));
            return;
        }

        BlockState blockState = playerBucketFillEvent.getBlock().getState();
        Location location = blockState.getLocation();
        Material brokenMaterial = blockState.getType();
        BlockType brokenBlockType = brokenMaterial.asBlockType();
        if(brokenBlockType == null) return;

        playerBucketFillEvent.setCancelled(true);

        // Replace the broken block
        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
            BlockState currentBlockState = location.getBlock().getState(false);
            currentBlockState.setType(brokenMaterial);
            currentBlockState.setBlockData(blockState.getBlockData());
            currentBlockState.update(true, false);
        }, 1L);

        blockDataByRegion.entrySet().stream()
                .filter(entry -> entry.getKey().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                .map(entry -> entry.getValue().stream()
                        .filter(packetBlock -> brokenBlockType.equals(packetBlock.worldType()))
                        .findFirst()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(packetBlock -> {
                    ItemStack itemStack = playerBucketFillEvent.getItemStack();
                    if(itemStack != null) {
                        PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                    }

                    BlockType replacementBlockType = packetBlock.replacementType();
                    Material replacementMaterial = replacementBlockType.asMaterial();

                    cooldownManager.addLocationCooldown(uuid, location, packetBlock.replacementType(), packetBlock.cooldownSeconds());

                    if (replacementMaterial != null) {
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                            if (player.isOnline() && player.isConnected()) {
                                player.sendBlockChange(location, replacementMaterial.createBlockData());
                            }
                        }, 2L);
                    }
                });
    }

    /**
     * Handles a {@link PlayerBucketEmptyEvent}.
     * Creative players are ignored.
     * This event is always cancelled otherwise.
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}
     */
    @Override
    public void handleBucketEmptied(@NotNull PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        Player player = playerBucketEmptyEvent.getPlayer();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        playerBucketEmptyEvent.setCancelled(true);
        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().canNotPlaceBlock()));
    }

    /**
     * Handles a {@link PlayerInteractEvent}.
     * Creative players are ignored.
     * Only handles right click interactions where the block is non-null and not air.
     * If the player has no time for the mine, a no access message is sent to the player.
     * If the block's location is on cooldown, a cooldown message is sent to the player.
     * If the block is not minable at all, a can not mine message is sent to the player.
     * The {@link PlayerInteractEvent} is cancelled in the above 3 scenarios.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @Override
    public void handlePlayerInteract(@NotNull PlayerInteractEvent playerInteractEvent) {
        if(mineId == null) return;
        Player player = playerInteractEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        Action action = playerInteractEvent.getAction();
        if(!action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block block = playerInteractEvent.getClickedBlock();
        if(block == null) return;
        Material blockMaterial = block.getType();
        if(blockMaterial.isAir()) return;

        if(!mineTimeManager.hasMineTime(uuid, mineId)) {
            playerInteractEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineAccessNoTime()));
            return;
        }

        Location location = block.getLocation();
        BlockType blockType = blockMaterial.asBlockType();
        if(blockType == null) return;

        if(isLocationOnCooldown(uuid, location)) {
            playerInteractEvent.setCancelled(true);
            sendBulkBlockUpdates(player, uuid);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().cooldown()));
        } else {
            if(!isBlockMineable(uuid, location, blockType)) {
                playerInteractEvent.setCancelled(true);
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().canNotBreakBlock()));
            }
        }
    }

    /**
     * Handles a {@link PlayerHarvestBlockEvent}.
     * Creative players are ignored.
     * If the player has no time for the mine, a no access message is sent to the player.
     * If the block's location is on cooldown, a cooldown message is sent to the player.
     * If the block is not minable at all, a can not mine message is sent to the player.
     * Otherwise, the appropriate items will be given to the player and the block state will be reverted.
     * @param playerHarvestBlockEvent A {@link PlayerHarvestBlockEvent}.
     */
    @Override
    public void handlePlayerHarvestBlockEvent(@NotNull PlayerHarvestBlockEvent playerHarvestBlockEvent) {
        if(mineId == null) return;
        Player player = playerHarvestBlockEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!mineTimeManager.hasMineTime(uuid, mineId)) {
            playerHarvestBlockEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().mineAccessNoTime()));
            return;
        }

        BlockState blockState = playerHarvestBlockEvent.getHarvestedBlock().getState();
        Location location = blockState.getLocation();
        Material harvestedMaterial = blockState.getType();
        BlockType harvestedBlockType = harvestedMaterial.asBlockType();
        if(harvestedBlockType == null) return;

        playerHarvestBlockEvent.setCancelled(true);

        blockDataByRegion.entrySet().stream()
                .filter(entry -> entry.getKey().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                .map(entry -> entry.getValue().stream()
                        .filter(packetBlock -> harvestedBlockType.equals(packetBlock.worldType()))
                        .findFirst()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(packetBlock -> {
                    // Replace the harvested block
                    skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                        BlockState currentBlockState = location.getBlock().getState(false);
                        currentBlockState.setType(harvestedMaterial);
                        currentBlockState.setBlockData(blockState.getBlockData());
                        currentBlockState.update(true, false);
                    }, 1L);

                    // Add harvested items to the player's inventory.
                    for (ItemStack harvestedItem : playerHarvestBlockEvent.getItemsHarvested()) {
                        PlayerUtil.giveItem(player.getInventory(), harvestedItem, harvestedItem.getAmount(), player.getLocation());
                    }

                    BlockType replacementBlockType = packetBlock.replacementType();
                    Material replacementMaterial = replacementBlockType.asMaterial();

                    cooldownManager.addLocationCooldown(uuid, location, packetBlock.replacementType(), packetBlock.cooldownSeconds());

                    if(replacementMaterial != null) {
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                            if (player.isOnline() && player.isConnected()) {
                                player.sendBlockChange(location, replacementMaterial.createBlockData());
                            }
                        }, 2L);
                    }
                });
    }

    /**
     * Handles a {@link BlockFertilizeEvent}
     * This method does nothing.
     * @param blockFertilizeEvent A {@link BlockFertilizeEvent}
     */
    @Override
    public void handleBlockFertilizeEvent(@NotNull BlockFertilizeEvent blockFertilizeEvent) {}

    /**
     * Handles a {@link StructureGrowEvent}
     * This method does nothing.
     * @param structureGrowEvent A {@link StructureGrowEvent}
     */
    @Override
    public void handleStructureGrowEvent(@NotNull StructureGrowEvent structureGrowEvent) {}

    /**
     * Handles an {@link EntityChangeBlockEvent}
     * This method does nothing.
     * @param entityChangeBlockEvent A {@link EntityChangeBlockEvent}
     */
    @Override
    public void handleEntityChangeBlockEvent(@NotNull EntityChangeBlockEvent entityChangeBlockEvent) {}

    /**
     * The event is cancelled regardless if a player initiated it or not.
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param blockExplodeEvent A {@link BlockExplodeEvent}
     */
    @Override
    public void handleBlockExplodeEvent(@Nullable Player player, @NotNull BlockExplodeEvent blockExplodeEvent) {
        blockExplodeEvent.setCancelled(true);
    }

    /**
     * The event is cancelled regardless if a player initiated it or not.
     * @param player The {@link Player} who initiated the explosion, or null.
     * @param entityExplodeEvent An {@link EntityExplodeEvent}
     */
    @Override
    public void handleEntityExplodeEvent(@Nullable Player player, @NotNull EntityExplodeEvent entityExplodeEvent) {
        entityExplodeEvent.setCancelled(true);
    }

    /**
     * Handles a {@link BlockFromToEvent}.
     * Cancels the movement of lava and water inside the mine.
     * @param blockFromToEvent A {@link BlockFromToEvent}.
     */
    @Override
    public void handleBlockFromToEvent(@NotNull BlockFromToEvent blockFromToEvent) {
        blockFromToEvent.setCancelled(true);
    }

    /**
     * Handles when a block is placed inside a mine.
     * Creative players are ignored.
     * Otherwise, the event is always cancelled and a message is sent to the player.
     * @param blockPlaceEvent A BlockPlaceEvent
     */
    @Override
    public void handleBlockPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
        Player player = blockPlaceEvent.getPlayer();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;

        blockPlaceEvent.setCancelled(true);
        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.packetMineMessages().canNotPlaceBlock()));
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
     * Handles when a player loads a chunk inside a mine and sends any block updates as needed.
     * @param playerChunkLoadEvent A {@link PlayerChunkLoadEvent}.
     */
    @Override
    public void handlePlayerChunkLoad(@NotNull PlayerChunkLoadEvent playerChunkLoadEvent) {
        Player player = playerChunkLoadEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Location location = player.getLocation();

        if(isLocationInMine(location)) {
            this.sendBulkBlockUpdates(player, uuid);
        }
    }

    /**
     * Create and show the boss bar for this mine to the player.
     * @param player The {@link Player} to show the boss bar.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void createAndShowBossBar(@NotNull Player player, @NotNull UUID uuid) {
        if(mineId == null) return;

        long mineTimeSeconds = mineTimeManager.getMineTime(uuid, mineId);
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", localeManager.getTimeMessage(mineTimeSeconds)));

        BossBar.Color bossBarColor;
        BossBar.Overlay bossBarOverlay;
        try {
            bossBarColor = BossBar.Color.valueOf(mineConfig.bossBar().color());
            bossBarOverlay = BossBar.Overlay.valueOf(mineConfig.bossBar().overlay());
        } catch (IllegalArgumentException e) {
            skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to show boss bar due to a configuration error. " + e.getMessage()));
            return;
        }

        BossBar bossBar;
        if(mineTimeSeconds > 0) {
            if(mineConfig.bossBar().timeText() == null) {
                skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to create a boss bar due to invalid boss bar time text."));
                return;
            }

            bossBar = BossBar.bossBar(AdventureUtil.serialize(mineConfig.bossBar().timeText(), placeholders), 1, bossBarColor, bossBarOverlay);
        } else {
            if(mineConfig.bossBar().noTimeText() == null) {
                skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to create a boss bar due to invalid boss bar no time text."));
                return;
            }

            bossBar = BossBar.bossBar(AdventureUtil.serialize(mineConfig.bossBar().noTimeText(), placeholders), 1, bossBarColor, bossBarOverlay);
        }

        bossBarManager.setBossBar(player, uuid, bossBar);
    }

    /**
     * Update the boss bar shown to the player.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void updateBossBar(@NotNull UUID uuid) {
        if(mineId == null) return;

        long mineTimeSeconds = mineTimeManager.getMineTime(uuid, mineId);
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", localeManager.getTimeMessage(mineTimeSeconds)));

        BossBar bossBar = bossBarManager.getBossBar(uuid);
        if(bossBar == null) return;

        if(mineTimeSeconds > 0) {
            if(mineConfig.bossBar().timeText() == null) {
                skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to update a boss bar due to invalid boss bar time text."));
                return;
            }

            bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().timeText(), placeholders));
        } else {
            if(mineConfig.bossBar().noTimeText() == null) {
                skyMines.getComponentLogger().warn(AdventureUtil.serialize("Unable to update a boss bar due to invalid boss bar no time text."));
                return;
            }

            bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().noTimeText(), placeholders));
        }
    }

    /**
     * Cleans up any data for this mine on unload.
     * Will remove any boss bars and revert any client-side block changes.
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

                    Map<Location, BlockData> blockDataMap = cooldownManager.getBlockDataOnCooldown(uuid);
                    List<BlockState> blockStates = new ArrayList<>();
                    blockDataMap.forEach((blockLocation, blockData) -> {
                        BlockState blockState = blockLocation.getBlock().getState(true);
                        blockStates.add(blockState);
                    });

                    if(!onDisable) {
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> player.sendBlockChanges(blockStates), 1L);
                    } else {
                        player.sendBlockChanges(blockStates);
                    }
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
     * Gets a WorldGuard region from the provided world and region name.
     * @param regionName The region name.
     * @return A ProtectedRegion or null if one was not found in that world and by that name.
     */
    private @Nullable ProtectedRegion getRegion(@Nullable String regionName) {
        if(regionManager == null) return null;
        if(regionName == null) return null;

        return regionManager.getRegion(regionName);
    }

    /**
     * Sends client-side block updates for blocks already mined.
     * @param player The {@link Player} to send block changes to.
     * @param uuid The {@link UUID} of the player.
     */
    private void sendBulkBlockUpdates(@NotNull Player player, @NotNull UUID uuid) {
        Map<Location, BlockData> blockDataMap = cooldownManager.getBlockDataOnCooldown(uuid);

        List<BlockState> blockStates = new ArrayList<>();
        blockDataMap.forEach((location, blockData) -> {
            BlockState blockState = location.getBlock().getState(true);
            BlockType blockType = blockData.getReplacementType();
            Material material = blockType.asMaterial();

            if(material != null) {
                blockState.setType(material);
                blockStates.add(blockState);
            }
        });

        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> player.sendBlockChanges(blockStates), 1L);
    }
}