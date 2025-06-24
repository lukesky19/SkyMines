package com.github.lukesky19.skymines.mine;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skymines.SkyMines;
import com.github.lukesky19.skymines.configuration.LocaleManager;
import com.github.lukesky19.skymines.data.MineBlock;
import com.github.lukesky19.skymines.data.PacketBlock;
import com.github.lukesky19.skymines.data.config.Locale;
import com.github.lukesky19.skymines.data.config.MineConfig;
import com.github.lukesky19.skymines.manager.DatabaseManager;
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
import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

/**
 * This class handles a Mine based on sending client's client-side block updates.
 * The actual blocks don't change in the world and the mined blocks are tracked server side.
 * The block only changes client-side or visually for the player.
 */
public class PacketMine extends Mine {
    // Class instances
    private final SkyMines skyMines;
    private final LocaleManager localeManager;
    private final DatabaseManager databaseManager;

    // Mine config
    private boolean status = true;
    private final MineConfig mineConfig;
    private String mineId;
    private RegionManager regionManager;
    private World mineWorld;
    private ProtectedRegion mineRegion;
    private final HashMap<ProtectedRegion, List<PacketBlock>> childRegions = new HashMap<>();

    // Player data
    private final HashMap<UUID, List<MineBlock>> playerMinedBlocks = new HashMap<>();
    private final HashMap<UUID, Integer> playerTimes = new HashMap<>();
    private final HashMap<UUID, BossBar> playerBossBars = new HashMap<>();

    // Tasks
    private BukkitTask timeLimitTask;
    private BukkitTask blockRevertTask;

    /**
     * Constructor.
     * Creates all necessary data to run the mine. Check {@link #isSetup()} to see if the Mine was created successfully or not.
     * @param skyMines The SkyMines' Plugin
     * @param localeManager A LocaleLoader instance.
     * @param databaseManager A DatabaseManager instance.
     * @param mineConfig The MineConfig for this Mine.
     */
    public PacketMine(SkyMines skyMines, LocaleManager localeManager, DatabaseManager databaseManager, MineConfig mineConfig) {
        ComponentLogger logger = skyMines.getComponentLogger();
        this.skyMines = skyMines;
        this.localeManager = localeManager;
        this.databaseManager = databaseManager;
        this.mineConfig = mineConfig;

        if(mineConfig.mineId() != null) {
            this.mineId = mineConfig.mineId();
        } else {
            logger.error(AdventureUtil.serialize("<red>Unable to create mine due to a null mine id.</red>"));
            status = false;
            return;
        }

        if(mineConfig.worldName() != null) {
            mineWorld = skyMines.getServer().getWorld(mineConfig.worldName());
            if(mineWorld != null) {
                regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(mineWorld));

                if(mineConfig.parentRegion() != null) {
                    ProtectedRegion parentRegion = getRegion(mineWorld, mineConfig.parentRegion());
                    if (parentRegion != null) {
                        this.mineRegion = parentRegion;
                    } else {
                        logger.error(AdventureUtil.serialize("<red>Unable to create mine due to region " + mineConfig.parentRegion() + " not being found.</red>"));
                        status = false;
                        return;
                    }
                } else {
                    logger.error(AdventureUtil.serialize("<red>Unable to create mine due to a parent region not being configured.</red>"));
                    status = false;
                    return;
                }

                for(MineConfig.ChildRegionData childRegionData : mineConfig.childRegions()) {
                    if(childRegionData.region() != null) {
                        ProtectedRegion childRegion = getRegion(mineWorld, childRegionData.region());
                        if(childRegion != null) {
                            // Parse blocks that can be mined
                            List<PacketBlock> packetBlocks = new ArrayList<>();
                            for(MineConfig.BlockData blockData : childRegionData.blocksAllowed()) {
                                if(blockData.block() != null && blockData.replacement() != null) {
                                    Material worldMaterial = Material.getMaterial(blockData.block());
                                    Material replacementMaterial = Material.getMaterial(blockData.replacement());
                                    if(worldMaterial != null && replacementMaterial != null) {
                                        PacketBlock packetBlock = new PacketBlock(worldMaterial, replacementMaterial, null, blockData.cooldownSeconds());
                                        if(blockData.lootTable() != null) {
                                            NamespacedKey key = NamespacedKey.fromString(blockData.lootTable());
                                            if(key != null) {
                                                LootTable lootTable = skyMines.getServer().getLootTable(key);
                                                if(lootTable != null) {
                                                    packetBlock = new PacketBlock(worldMaterial, replacementMaterial, lootTable, blockData.cooldownSeconds());
                                                } else {
                                                    logger.warn(AdventureUtil.serialize("No loot table found for key " + key.asString()));
                                                }
                                            } else {
                                                logger.warn(AdventureUtil.serialize("Unable to get loot table due to a null NamespacedKey."));
                                            }
                                        }

                                        packetBlocks.add(packetBlock);
                                    } else {
                                        if(worldMaterial == null) {
                                            logger.warn(AdventureUtil.serialize("Unable to find a material of name " + blockData.block()));
                                        } else {
                                            logger.warn(AdventureUtil.serialize("Unable to find a material of name " + blockData.replacement()));
                                        }
                                    }
                                }
                            }

                            childRegions.put(childRegion, packetBlocks);
                        } else {
                            logger.warn(AdventureUtil.serialize("Unable to find a region by the name of " + childRegionData.region()));
                        }
                    } else {
                        logger.warn(AdventureUtil.serialize("There is a child region that has a null region name."));
                    }
                }
            } else {
                logger.error(AdventureUtil.serialize("<red>Unable to create mine due to world " + mineConfig.worldName() + " not being found.</red>"));
                status = false;
                return;
            }
        } else {
            logger.error(AdventureUtil.serialize("<red>Unable to create mine due to a world name not being configured.</red>"));
            status = false;
            return;
        }

        try {
            playerTimes.putAll(databaseManager.getPlayerTimesByMineId(mineId));
        } catch (SQLException e) {
            logger.warn(AdventureUtil.serialize("Unable to load player times from database. Error:"));
            logger.error(AdventureUtil.serialize(e.getMessage()));
            return;
        }

        // If the plugin is somehow loaded with players online (plugman), show boss bars for players in the mine
        for(Player player : skyMines.getServer().getOnlinePlayers()) {
            if(isLocationInMine(player.getLocation())) {
                BossBar bossBar = getBossBar(player.getUniqueId());
                if(bossBar != null) {
                    player.showBossBar(bossBar);
                }
            }
        }

        startBlockRevertTask();
        startTimeLimitTask();
    }

    @Override
    public @NotNull String getMineId() {
        return mineId;
    }

    @Override
    public boolean isLocationInMine(@NotNull Location location) {
        return mineWorld.equals(location.getWorld()) && mineRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Checks if the Player has time for this mine and if the player can mine the block.
     * @param player The player mining the block.
     * @param uuid The UUID of the player.
     * @param location The location of the block.
     * @param material The material of the block.
     * @return true if the player can mine, false if not.
     */
    public boolean isBlockMineable(@NotNull Player player, @NotNull UUID uuid, @NotNull Location location, @NotNull Material material) {
        if(!playerTimes.containsKey(uuid)) return false;

        for (Map.Entry<ProtectedRegion, List<PacketBlock>> regionEntry : childRegions.entrySet()) {
            ProtectedRegion region = regionEntry.getKey();
            if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                for (PacketBlock packetBlock : regionEntry.getValue()) {
                    if (packetBlock.worldType().equals(material)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean isBlockOnCooldown(@NotNull UUID uuid, @NotNull Location location) {
        List<MineBlock> blockLocations = playerMinedBlocks.get(uuid);
        if(blockLocations != null) {
            for(MineBlock mineBlock : blockLocations) {
                if(mineBlock.getLocation().equals(location)) return true;
            }
        }

        return false;
    }

    /**
     * Checks if the player has time to access the mine, if the block is on cooldown or if the block is mineable.
     * Players in creative mode will be ignored.
     * @param blockBreakEvent A BlockBreakEvent
     */
    @Override
    public void handleBlockBreak(@NotNull BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!playerTimes.containsKey(uuid)) {
            blockBreakEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.mineNoAccess()));
            return;
        }

        Block block = blockBreakEvent.getBlock();
        Location location = block.getLocation();
        Material material = block.getType();

        if(this.isBlockOnCooldown(uuid, location)) {
            blockBreakEvent.setCancelled(true);
            this.sendBulkBlockUpdates(player, uuid);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.cooldown()));
        } else {
            if(!this.isBlockMineable(player, uuid, location, material)) {
                blockBreakEvent.setCancelled(true);
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.canNotMine()));
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
        Player player = blockDropItemEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!playerTimes.containsKey(uuid)) {
            blockDropItemEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.mineNoAccess()));
            return;
        }

        BlockState blockState = blockDropItemEvent.getBlockState();
        Location location = blockState.getLocation();
        Material brokenBlockMaterial = blockState.getType();

        for(Map.Entry<ProtectedRegion, List<PacketBlock>> regionEntry : childRegions.entrySet()) {
            ProtectedRegion region = regionEntry.getKey();

            if(region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                for(PacketBlock packetBlock : regionEntry.getValue()) {
                    Material allowedMaterial = packetBlock.worldType();
                    Material replacementMaterial = packetBlock.replacementType();
                    int delaySeconds = packetBlock.cooldownSeconds();

                    if(brokenBlockMaterial.equals(allowedMaterial)) {
                        blockDropItemEvent.setCancelled(true);

                        if(blockState instanceof BrushableBlock brushableBlock) {
                            LootTable lootTable = packetBlock.lootTable();
                            if(lootTable != null) {
                                brushableBlock.setLootTable(lootTable);
                            }
                        }

                        // Replace broken block
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () ->
                                blockState.update(true, false), 1L);

                        for(Item item : blockDropItemEvent.getItems()) {
                            ItemStack itemStack = item.getItemStack();
                            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                        }

                        List<MineBlock> mineBlocks = playerMinedBlocks.getOrDefault(uuid, new ArrayList<>());
                        mineBlocks.add(new MineBlock(location, replacementMaterial, delaySeconds));

                        playerMinedBlocks.put(uuid, mineBlocks);

                        // Send the block change
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                            if(player.isOnline() && player.isConnected()) {
                                player.sendBlockChange(location, replacementMaterial.createBlockData());
                            }
                        }, 2L);
                    }
                }
            }
        }
    }

    /**
     * Checks if a player has time to access the mine, if the clicked block is on cooldown, or if the clicked block can be mined.
     * Players in creative mode will be ignored.
     * @param playerInteractEvent A PlayerInteractEvent
     */
    @Override
    public void handlePlayerInteract(@NotNull PlayerInteractEvent playerInteractEvent) {
        Player player = playerInteractEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();
        Block block = playerInteractEvent.getClickedBlock();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(block == null ) return;
        if(block.getType().equals(Material.AIR)) return;
        if(!playerTimes.containsKey(uuid)) {
            playerInteractEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.mineNoAccess()));
            return;
        }

        Location location = block.getLocation();
        if(isBlockOnCooldown(uuid, location)) {
            playerInteractEvent.setCancelled(true);
            this.sendBulkBlockUpdates(player, uuid);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.cooldown()));
        } else {
            if(!this.isBlockMineable(player, uuid, location, block.getType())) {
                playerInteractEvent.setCancelled(true);
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.canNotMine()));
            }
        }
    }

    /**
     * Checks if the player has time to access the mine and if the block can be harvested.
     * If the block can be harvested, the block is replaced, the items given to the player, and sends client-side block updates.
     * Players in creative mode will be ignored.
     * @param playerHarvestBlockEvent A PlayerHarvestBlockEvent
     */
    @Override
    public void handlePlayerHarvestBlockEvent(@NotNull PlayerHarvestBlockEvent playerHarvestBlockEvent) {
        Player player = playerHarvestBlockEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Locale locale = localeManager.getLocale();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!playerTimes.containsKey(uuid)) {
            playerHarvestBlockEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.mineNoAccess()));
            return;
        }

        BlockState blockState = playerHarvestBlockEvent.getHarvestedBlock().getState();
        Location location = blockState.getLocation();
        Material brokenBlockMaterial = blockState.getType();

        for(Map.Entry<ProtectedRegion, List<PacketBlock>> regionEntry : childRegions.entrySet()) {
            ProtectedRegion region = regionEntry.getKey();

            if(region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                for(PacketBlock packetBlock : regionEntry.getValue()) {
                    Material allowedMaterial = packetBlock.worldType();
                    Material replacementMaterial = packetBlock.replacementType();
                    int delaySeconds = packetBlock.cooldownSeconds();

                    if(brokenBlockMaterial.equals(allowedMaterial)) {
                        playerHarvestBlockEvent.setCancelled(true);

                        if(blockState instanceof BrushableBlock brushableBlock) {
                            LootTable lootTable = packetBlock.lootTable();
                            if(lootTable != null) {
                                brushableBlock.setLootTable(lootTable);
                            }
                        }

                        // Replace broken block
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () ->
                                blockState.update(true, false), 1L);

                        for(ItemStack itemStack : playerHarvestBlockEvent.getItemsHarvested()) {
                            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                        }

                        List<MineBlock> mineBlocks = playerMinedBlocks.getOrDefault(uuid, new ArrayList<>());
                        mineBlocks.add(new MineBlock(location, replacementMaterial, delaySeconds));

                        playerMinedBlocks.put(uuid, mineBlocks);

                        // Send the block change
                        skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> {
                            if(player.isOnline() && player.isConnected()) {
                                player.sendBlockChange(location, replacementMaterial.createBlockData());
                            }
                        }, 2L);
                    }
                }
            }
        }
    }

    /**
     * Cancels the block place event and sends the player a message that they cannot place blocks in mines/
     * Players in creative mode will be ignored.
     * @param blockPlaceEvent A BlockPlaceEvent
     */
    @Override
    public void handleBlockPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
        Player player = blockPlaceEvent.getPlayer();
        Locale locale = localeManager.getLocale();

        if(blockPlaceEvent.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        blockPlaceEvent.setCancelled(true);
        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.mineNoPlace()));
    }

    /**
     * Checks the location the player moved from and to, checking if that location is inside a mine.
     * Show the boss bar if they are in said mine or remove it if not.
     * @param playerMoveEvent A PlayerMoveEvent
     */
    @Override
    public void handlePlayerMoveEvent(@NotNull PlayerMoveEvent playerMoveEvent) {
        Location from = playerMoveEvent.getFrom();
        Location to = playerMoveEvent.getTo();

        Player player = playerMoveEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(isLocationInMine(from) && !isLocationInMine(to)) {
            BossBar bossBar = getBossBar(uuid);
            if(bossBar != null) {
                player.hideBossBar(bossBar);
            }
        } else if(!isLocationInMine(from) && isLocationInMine(to)) {
            BossBar bossBar = getBossBar(uuid);
            if(bossBar != null) {
                player.showBossBar(bossBar);
            }
        }
    }

    /**
     * Checks the location the player teleported from and to, checking if that location is inside a mine.
     * Show the boss bar if they are in said mine, or remove it if not.
     * @param playerTeleportEvent A PlayerTeleportEvent
     */
    @Override
    public void handlePlayerTeleportEvent(@NotNull PlayerTeleportEvent playerTeleportEvent) {
        Location from = playerTeleportEvent.getFrom();
        Location to = playerTeleportEvent.getTo();

        Player player = playerTeleportEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        BossBar bossBar = getBossBar(uuid);
        if(bossBar != null) {
            if(isLocationInMine(from) && !isLocationInMine(to)) {
                player.hideBossBar(bossBar);
            } else if(!isLocationInMine(from) && isLocationInMine(to)) {
                player.showBossBar(bossBar);
            }
        }
    }

    /**
     * Checks if the player is inside a mine when they login. If they are, show the boss bar.
     * @param playerJoinEvent A PlayerJoinEvent
     */
    @Override
    public void handlePlayerJoinEvent(@NotNull PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        BossBar bossBar = getBossBar(uuid);
        if(bossBar != null) {
            if(isLocationInMine(player.getLocation())) {
                player.showBossBar(bossBar);
            }
        }
    }

    /**
     * Removes the stored boss bar for the player that logged out.
     * @param playerQuitEvent A PlayerQuitEvent
     */
    @Override
    public void handlePlayerQuitEvent(@NotNull PlayerQuitEvent playerQuitEvent) {
        playerBossBars.remove(playerQuitEvent.getPlayer().getUniqueId());
    }

    /**
     * If a chunk inside a mine is loaded, send block updates for already mined blocks.
     * @param playerChunkLoadEvent A PlayerChunkLoadEvent
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

    @Override
    public int addPlayerTime(@NotNull Player player, @NotNull UUID uuid, int time) {
        Integer playerTime = playerTimes.get(uuid);
        if(playerTime != null) {
            playerTime = playerTime + time;
            playerTimes.put(uuid, playerTime);

            databaseManager.setPlayerTime(mineId, uuid.toString(), playerTime);

            BossBar bossBar = getBossBar(uuid);
            if(bossBar != null) {
                if(isLocationInMine(player.getLocation())) {
                    player.showBossBar(bossBar);
                }
            }

            return playerTime;
        } else {
            playerTimes.put(uuid, time);

            databaseManager.insertPlayerTime(mineId, uuid.toString(), time);

            BossBar bossBar = getBossBar(uuid);
            if(bossBar != null) {
                if(isLocationInMine(player.getLocation())) {
                    player.showBossBar(bossBar);
                }
            }

            return time;
        }
    }

    @Override
    public int removePlayerTime(@NotNull Player player, @NotNull UUID uuid, int time) {
        Integer playerTime = playerTimes.get(uuid);
        if(playerTime != null) {
            playerTime = playerTime - time;

            if(playerTime > 0) {
                playerTimes.put(uuid, playerTime);

                databaseManager.setPlayerTime(mineId, uuid.toString(), playerTime);
            } else {
                playerTimes.remove(uuid);

                databaseManager.deletePlayerTime(mineId, uuid.toString());
            }

            BossBar bossBar = getBossBar(uuid);
            if (bossBar != null) {
                if (isLocationInMine(player.getLocation())) {
                    player.showBossBar(bossBar);
                }
            }

            return playerTime;
        } else {
            return 0;
        }
    }

    @Override
    public int setPlayerTime(@NotNull Player player, @NotNull UUID uuid, int time) {
        playerTimes.put(uuid, time);

        databaseManager.setPlayerTime(mineId, uuid.toString(), time);

        BossBar bossBar = getBossBar(uuid);
        if(bossBar != null) {
            if(isLocationInMine(player.getLocation())) {
                player.showBossBar(bossBar);
            }
        }

        return time;
    }

    @Override
    public @Nullable Integer getPlayerTime(@NotNull UUID uuid) {
        return playerTimes.get(uuid);
    }

    @Override
    public void cleanUp() {
        stopBlockRevertTask();
        stopTimeLimitTask();

        for(Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            UUID uuid = entry.getKey();
            Player player = skyMines.getServer().getPlayer(uuid);
            if(player != null && player.isOnline() && player.isConnected()) {
                player.hideBossBar(entry.getValue());

                List<MineBlock> mineBlocks = playerMinedBlocks.get(uuid);
                if(mineBlocks != null) {
                    for (MineBlock mineBlock : playerMinedBlocks.get(uuid)) {
                        player.sendBlockChange(mineBlock.getLocation(), mineBlock.getLocation().getBlock().getBlockData());
                    }
                }
            }
        }
    }

    @Override
    public boolean isSetup() {
        return status;
    }

    /**
     * Starts the task to revert client-side blocks.
     */
    private void startBlockRevertTask() {
        blockRevertTask = skyMines.getServer().getScheduler().runTaskTimerAsynchronously(skyMines, () -> {
            for(Map.Entry<UUID, List<MineBlock>> entry : playerMinedBlocks.entrySet()) {
                UUID uuid = entry.getKey();
                List<MineBlock> mineBlocks = entry.getValue();
                Iterator<MineBlock> iterator = mineBlocks.iterator();
                List<MineBlock> updatedMineBlocks = new ArrayList<>();

                while(iterator.hasNext()) {
                    MineBlock mineBlock = iterator.next();
                    int time = mineBlock.getCooldownSeconds();
                    time--;

                    if(time <= 0) {
                        // Schedule the block change on the main thread
                        skyMines.getServer().getScheduler().runTask(skyMines, () -> {
                            Player player = skyMines.getServer().getPlayer(uuid);
                            if (player != null && player.isOnline() && player.isConnected() && mineBlock.getLocation().isChunkLoaded()) {
                                player.sendBlockChange(mineBlock.getLocation(), mineBlock.getLocation().getBlock().getBlockData());
                            }
                        });
                    } else {
                        mineBlock.setCooldownSeconds(time);
                        updatedMineBlocks.add(mineBlock);
                    }
                }

                mineBlocks.clear();
                mineBlocks.addAll(updatedMineBlocks);

                if (mineBlocks.isEmpty()) {
                    playerMinedBlocks.remove(uuid);
                } else {
                    playerMinedBlocks.put(uuid, mineBlocks);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Stops the task to revert client-side blocks.
     */
    private void stopBlockRevertTask() {
        if(blockRevertTask != null && !blockRevertTask.isCancelled()) {
            blockRevertTask.cancel();
            blockRevertTask = null;
        }
    }

    /**
     * Starts the task that tracks the time the player can access the mine for.
     */
    private void startTimeLimitTask() {
        timeLimitTask = skyMines.getServer().getScheduler().runTaskTimer(skyMines, () -> {
            Iterator<Map.Entry<UUID, Integer>> iterator = playerTimes.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID uuid = entry.getKey();
                Player player = skyMines.getServer().getPlayer(uuid);

                if(player != null && player.isOnline() && player.isConnected()) {
                    if(isLocationInMine(player.getLocation())) {
                        int time = entry.getValue();
                        time--;

                        if(time <= 0) {
                            iterator.remove();

                            databaseManager.deletePlayerTime(mineId, uuid.toString());

                            BossBar bossBar = playerBossBars.get(uuid);
                            if(bossBar != null) {
                                if(mineConfig.bossBar().noTimeText() != null) {
                                    bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().noTimeText()));
                                } else {
                                    player.hideBossBar(bossBar);
                                    playerBossBars.remove(uuid);
                                }
                            }
                        } else {
                            playerTimes.put(uuid, time);

                            databaseManager.setPlayerTime(mineId, uuid.toString(), time);

                            BossBar bossBar = playerBossBars.get(uuid);
                            if(bossBar != null) {
                                if(mineConfig.bossBar().timeText() != null) {
                                    String message = localeManager.getTimeMessage(time);

                                    List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", message));
                                    bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().timeText(), placeholders));
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    /**
     * Stops the task that tracks the time the player can access the mine for.
     */
    private void stopTimeLimitTask() {
        if(timeLimitTask != null && !timeLimitTask.isCancelled()) {
            timeLimitTask.cancel();
            timeLimitTask = null;
        }
    }

    /**
     * Sends client-side block updates for blocks already mined.
     * @param player The player.
     * @param uuid The UUID of the player.
     */
    private void sendBulkBlockUpdates(Player player, UUID uuid) {
        List<MineBlock> mineBlocks = playerMinedBlocks.get(uuid);
        if(mineBlocks != null && !mineBlocks.isEmpty()) {
            List<BlockState> blockStates = new ArrayList<>();
            for(MineBlock mineBlock : mineBlocks) {
                BlockState blockState = mineBlock.getLocation().getBlock().getState(true);
                blockState.setType(mineBlock.getReplacementMaterial());
                blockStates.add(blockState);
            }

            skyMines.getServer().getScheduler().runTaskLater(skyMines, () -> player.sendBlockChanges(blockStates), 1L);
        }
    }

    /**
     * Gets a WorldGuard region from the provided world and region name.
     * @param world The Bukkit World the region is in.
     * @param regionName The region name.
     * @return A ProtectedRegion or null if one was not found in that world and by that name.
     */
    @Nullable
    private ProtectedRegion getRegion(@NotNull World world, @NotNull String regionName) {
        if(regionManager != null) {
            return regionManager.getRegion(regionName);
        } else {
            throw new RuntimeException("Unable to find region due to a null RegionManager. Is the world name " + world.getName() + " a valid world?");
        }
    }

    /**
     * Gets the existing boss bar or creates a new one if not disabled by the config.
     * @param uuid The UUID of the player.
     * @return The Boss Bar or null.
     */
    @Nullable
    private BossBar getBossBar(@NotNull UUID uuid) {
        Integer time = playerTimes.get(uuid);
        BossBar bossBar = playerBossBars.get(uuid);

        if(time != null) {
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", localeManager.getTimeMessage(time)));
            if(bossBar != null) {
                if(mineConfig.bossBar().timeText() != null) {
                    bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().timeText(), placeholders));
                    playerBossBars.put(uuid, bossBar);

                    return bossBar;
                }
            } else {
                if(mineConfig.bossBar().timeText() != null) {
                    BossBar.Color bossBarColor = BossBar.Color.valueOf(mineConfig.bossBar().color());
                    BossBar.Overlay bossBarOverlay = BossBar.Overlay.valueOf(mineConfig.bossBar().overlay());

                    bossBar = BossBar.bossBar(AdventureUtil.serialize(mineConfig.bossBar().timeText(), placeholders), 1, bossBarColor, bossBarOverlay);
                    playerBossBars.put(uuid, bossBar);

                    return bossBar;
                }
            }
        } else {
            if(bossBar != null) {
                if(mineConfig.bossBar().noTimeText() != null) {
                    bossBar.name(AdventureUtil.serialize(mineConfig.bossBar().noTimeText()));
                    return bossBar;
                }
            } else {
                if(mineConfig.bossBar().noTimeText() != null) {
                    BossBar.Color bossBarColor = BossBar.Color.valueOf(mineConfig.bossBar().color());
                    BossBar.Overlay bossBarOverlay = BossBar.Overlay.valueOf(mineConfig.bossBar().overlay());

                    bossBar = BossBar.bossBar(AdventureUtil.serialize(mineConfig.bossBar().noTimeText()), 1, bossBarColor, bossBarOverlay);
                    playerBossBars.put(uuid, bossBar);

                    return bossBar;
                }
            }
        }

        return null;
    }
}
