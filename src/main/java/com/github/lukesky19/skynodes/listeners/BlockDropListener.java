/*
    SkyNodes tracks blocks broken in specific regions (nodes), replaces them, gives items, and sends client-side block changes.
    Copyright (C) 2023-2024  lukeskywlker19

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
package com.github.lukesky19.skynodes.listeners;

import com.github.lukesky19.skynodes.SkyNodes;
import com.github.lukesky19.skynodes.configuration.loader.LocaleLoader;
import com.github.lukesky19.skynodes.configuration.loader.NodeLoader;
import com.github.lukesky19.skynodes.configuration.record.Locale;
import com.github.lukesky19.skynodes.configuration.record.Node;
import com.github.lukesky19.skynodes.manager.NodeManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages listening to block drops and managing those blocks and drops for configured nodes.
 */
public class BlockDropListener implements Listener {
    final SkyNodes skyNodes;
    final LocaleLoader localeLoader;
    final NodeLoader nodeLoader;
    final NodeManager nodeManager;

    /**
     * Constructor
     * @param skyNodes Plugin Instance.
     * @param localeLoader LocaleLoader Instance.
     * @param nodeLoader NodeLoader Instance.
     * @param nodeManager NodeManager Instance.
     */
    public BlockDropListener(
            SkyNodes skyNodes,
            LocaleLoader localeLoader,
            NodeLoader nodeLoader,
            NodeManager nodeManager) {
        this.skyNodes = skyNodes;
        this.localeLoader = localeLoader;
        this.nodeLoader = nodeLoader;
        this.nodeManager = nodeManager;
    }

    /**
     * Listens to when an item is dropped by a block.<br>
     * Will add the items to the player's inventory or drop them in the world if:<br>
     * 1. The block is in a world listed in the nodes.yml config<br>
     * 2. The block is in the parent region of a mine. (And the region is not invalid (null)).<br>
     * 3. The block is in the child region of a particular node. (And the region is not invalid (null)).<br>
     * 4. The replacement material not invalid (null).<br>
     * 5. The material of the block broken matches one in particular node's block-allowed list.
     * @param event BlockDropItemEvent
     */
    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        MiniMessage mm = MiniMessage.miniMessage();
        Player player = event.getPlayer();
        if(!skyNodes.isPluginEnabled()) {
            player.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
            player.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>In order to protect any mines, all block drops will be cancelled.</red>"));
            player.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>Please report this to your system administrator.</red>"));
            event.setCancelled(true);
            return;
        }

        Node node = nodeLoader.getNodes();
        Locale locale = localeLoader.getLocale();
        World world = event.getBlockState().getWorld();
        Chunk chunk = event.getBlockState().getChunk();
        Location location = event.getBlockState().getLocation();
        BlockState state = event.getBlockState();
        BlockData data;
        Material material = state.getBlockData().getMaterial();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if(regions == null) return;

        LinkedHashMap<String, LinkedHashMap<Integer, Node.NodeData>> regionNodeDataMap = node.data().get(world.getName());
        if(regionNodeDataMap == null) return;

        // Loop through all the parent regions of the mines.
        for(Map.Entry<String, LinkedHashMap<Integer, Node.NodeData>> regionEntry : regionNodeDataMap.entrySet()) {
            // Get the parent region and check if it's not null.
            // If it is, send an error message.
            ProtectedRegion parentRegion = regions.getRegion(regionEntry.getKey());
            if(parentRegion != null) {
                // Get all regions that contain the block that was broken.
                ApplicableRegionSet regionSet = regions.getApplicableRegions(BlockVector3.at(state.getX(), state.getY(), state.getZ()));
                // Loop through those regions
                for(ProtectedRegion rg : regionSet) {
                    // Check if the parent region id matches a region id from above.
                    if(rg.getId().equals(parentRegion.getId())) {
                        // Loop throough all nodes for that parent region.
                        for(Map.Entry<Integer, Node.NodeData> nodeEntry : regionEntry.getValue().entrySet()) {
                            // Get the NodeData (config).
                            int nodeId = nodeEntry.getKey();
                            Node.NodeData nodeData = nodeEntry.getValue();
                            // Get the child region that contains the mineable area/node and check if it's not null.
                            // If it is, send an error message.
                            ProtectedRegion childRegion = regions.getRegion(nodeData.region());
                            if(childRegion != null) {
                                // Loop through those regions (again).
                                for(ProtectedRegion reg : regionSet) {
                                    // Check if the child region id matches a region id from above.
                                    if(reg.getId().equals(childRegion.getId())) {
                                        // Loop through all of the data in the blocks-allowed list for this particular node.
                                        for(Map.Entry<Integer, Node.BlockData> blockEntry : nodeData.blocksAllowed().entrySet()) {
                                            // Get the BlockData (config).
                                            int blockId = blockEntry.getKey();
                                            Node.BlockData blockData = blockEntry.getValue();
                                            // Get the configured Material that is allowed to be mined.
                                            Material blockMat = Material.getMaterial(blockData.block());
                                            // Get the configured replacement Material for that block.
                                            Material replacementMat = Material.getMaterial(blockData.replacement());
                                            // Check if the block Material is not null, or send an error message if so.
                                            if(blockMat != null) {
                                                // Check if the material of the block broken matches the configured material
                                                if(material.equals(blockMat)) {
                                                    // Cancel the event
                                                    event.setCancelled(true);
                                                    // Get the list of items that would of been dropped.
                                                    @NotNull List<Item> items = event.getItems();

                                                    // Replace the block that was broken.
                                                    Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
                                                        state.setType(blockMat);
                                                        state.update(true);
                                                    }, 1L);

                                                    // Save the BlockData which will be used to update the BlockState client-side after we are done tracking it.
                                                    data = state.getBlockData();

                                                    // If the BlockState is a BrushableBlock (Suspicious Sand or Gravel), set the loot table if configured.
                                                    if(state instanceof BrushableBlock brushableBlock) {
                                                        // Check if loot table is configured, if so, set it.
                                                        if(blockData.lootTable() != null) {
                                                            NamespacedKey key = NamespacedKey.fromString(blockData.lootTable(), null);

                                                            // Get the loot table and set the block's loot table if found.
                                                            LootTable lootTable = skyNodes.getServer().getLootTable(key);
                                                            if (lootTable != null) {
                                                                // Update the block's loot table and update the block in the world
                                                                Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
                                                                    brushableBlock.setLootTable(lootTable);
                                                                    brushableBlock.update(true);
                                                                }, 1L);

                                                                // Save the BlockData which will be used to update the BlockState client-side after we are done tracking it.
                                                                data = brushableBlock.getBlockData();
                                                            }
                                                        }
                                                    }

                                                    // Check if the replacement Material is not null, or send an error message if so.
                                                    if(replacementMat != null) {
                                                        // Set the BlockState to the replacement Material.
                                                        // This will not change the state in the world because we didn't call BlockState#update().
                                                        Bukkit.getScheduler().runTaskLater(skyNodes, () -> state.setBlockData(replacementMat.createBlockData()), 1L);
                                                        // Schedule client-side block changes and giving of items, or sending a cooldown message for BlockStates that are BrushableBlocks (Suspicious Sand/Gravel)
                                                        // The cooldown message is not sent for every block because it will spam the player when breaking something like Wheat or Carrots.
                                                        BlockData finalData = data;
                                                        Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
                                                            // If the block is not tracked, we'll track it, give out the items, and change the player's block client-side.
                                                            // Otherwise we'll set the BlockState back to the world's state since we changed it with the replacement Material above.
                                                            // Again, this will not change the state in the world because we didn't call BlockState#update().
                                                            if(!nodeManager.isBlockTracked(player, world, chunk, state)) {
                                                                if(blockData.delaySeconds() != null) {
                                                                    // Schedule the client-side block change.
                                                                    nodeManager.scheduleBlockChange(player, world, chunk, state);
                                                                    // Schedule the client-side block change to revert the previous change.
                                                                    nodeManager.scheduleBlockRevert(player, world, chunk, state, finalData, blockData.delaySeconds());

                                                                    // Give out the items either to the player's inventory or dropping them in the world.
                                                                    for (Item single : items) {
                                                                        if (player.getInventory().firstEmpty() != -1) {
                                                                            player.getInventory().addItem(single.getItemStack());
                                                                        } else {
                                                                            player.getWorld().dropItem(location, single.getItemStack());
                                                                        }
                                                                    }
                                                                } else {
                                                                    player.sendMessage(mm.deserialize(locale.prefix() + locale.invalidDelaySeconds(),
                                                                            Placeholder.parsed("blockid", String.valueOf(blockId)),
                                                                            Placeholder.parsed("nodeid", String.valueOf(nodeId))));

                                                                    // There was an error so we return
                                                                    return;
                                                                }
                                                            } else {
                                                                // Set the BlockState back to the world's state.
                                                                Bukkit.getScheduler().runTaskLater(skyNodes, () -> state.setBlockData(finalData), 1L);
                                                                // Send the cooldown message if the BlockState matches that of a Brushable Block (Suspicious Sand/Gravel).
                                                                Bukkit.getScheduler().runTaskLater(skyNodes, () -> {
                                                                    if(state instanceof BrushableBlock) {
                                                                        player.sendMessage(mm.deserialize(locale.prefix() + locale.cooldown()));
                                                                    }
                                                                }, 1L);
                                                            }
                                                        }, 1L);

                                                        // Since we met all our checks above, we don't want to continue looping through anything after this, so we call a return to exit the loops.
                                                        return;
                                                    } else {
                                                        player.sendMessage(mm.deserialize(locale.prefix() + locale.invalidReplacementMaterial(),
                                                                Placeholder.parsed("blockid", String.valueOf(blockId)),
                                                                Placeholder.parsed("nodeid", String.valueOf(nodeId))));
                                                        return;
                                                    }
                                                }
                                            } else {
                                                player.sendMessage(mm.deserialize(locale.prefix() + locale.invalidBlockMaterial(),
                                                        Placeholder.parsed("blockid", String.valueOf(blockId)),
                                                        Placeholder.parsed("nodeid", String.valueOf(nodeId))));
                                                return;
                                            }
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage(mm.deserialize(locale.prefix() + locale.invalidChildRegion(),
                                        Placeholder.parsed("nodeid", String.valueOf(nodeId))));
                                return;
                            }
                        }
                    }
                }
            } else {
                player.sendMessage(locale.prefix() + locale.invalidParentRegion());
                return;
            }
        }
    }
}
