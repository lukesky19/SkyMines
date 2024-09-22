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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class manages listening to blocks broken in nodes and cancels the event if that block isn't configured to be broken.
 */
public class BlockBreakListener implements Listener {
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
    public BlockBreakListener(
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
     * Listens to when a block is broken.<br>
     * Allows a block to be mined or not, sending an error message based on:<br>
     * 1. The block is in a world listed in the nodes.yml config<br>
     * 2. The block is in the parent region of a mine. (And the region is not invalid (null)).<br>
     * 3. The block is in the child region of a particular node. (And the region is not invalid (null)).<br>
     * 4. The material of the block broken matches one in particular node's block-allowed list.<br>
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        MiniMessage mm = MiniMessage.miniMessage();
        Player player = event.getPlayer();
        if(!skyNodes.isPluginEnabled()) {
            player.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>The plugin is currently is soft-disabled due to a configuration error.</red>"));
            player.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>In order to protect any mines, all block breaking will be cancelled.</red>"));
            player.sendMessage(mm.deserialize("<gray>[</gray><yellow>SkyNodes</yellow><gray>]</gray> <red>Please report this to your system administrator.</red>"));
            event.setCancelled(true);
            return;
        }

        boolean cancel = true;

        Node node = nodeLoader.getNodes();
        Locale locale = localeLoader.getLocale();
        Block block = event.getBlock();
        World world = block.getWorld();
        Material material = block.getType();

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
                ApplicableRegionSet regionSet = regions.getApplicableRegions(BlockVector3.at(block.getX(), block.getY(), block.getZ()));
                // Loop through those regions
                for(ProtectedRegion rg : regionSet) {
                    // Check if the parent region id matches a region id from above.
                    if(rg.getId().equals(parentRegion.getId())) {
                        // Loop through all nodes for that parent region.
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
                                            Node.BlockData blockData = blockEntry.getValue();
                                            // If the Material of the block broken is allowed to be broken, we won't cancel the event.
                                            if (material.equals(Material.getMaterial(blockData.block()))) {
                                                cancel = false;
                                            }
                                        }

                                        // If the Material of the block broken isn't allowed to be broken, we cancel the event and send a message to the player.
                                        if(cancel) {
                                            event.setCancelled(true);
                                            player.sendMessage(mm.deserialize(locale.prefix() + locale.canNotMine()));
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage(mm.deserialize(locale.prefix() + locale.invalidChildRegion(), Placeholder.parsed("nodeid", String.valueOf(nodeId))));
                            }
                        }
                    }
                }
            } else {
                player.sendMessage(mm.deserialize(locale.prefix() + locale.invalidParentRegion()));
            }
        }
    }
}
