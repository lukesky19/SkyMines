/*
    SkyNodes places a random configured schematic after a set period of time.
    Copyright (C) 2023  lukeskywlker19

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
import com.github.lukesky19.skynodes.managers.*;
import com.github.lukesky19.skynodes.records.Messages;
import com.github.lukesky19.skynodes.records.Settings;
import com.github.lukesky19.skynodes.records.SkyNode;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.*;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class SkyNodeBlockBreakListener implements Listener {
    public SkyNodeBlockBreakListener(SkyNodes plugin, MessagesManager messagesManager, SettingsManager settingsManager, SkyNodeManager skyNodeManager) {
        this.plugin = plugin;
        this.messagesManager = messagesManager;
        this.settingsManager = settingsManager;
        this.skyNodeManager = skyNodeManager;
    }
    final SkyNodes plugin;
    final SkyNodeManager skyNodeManager;
    final MessagesManager messagesManager;
    final SettingsManager settingsManager;

    @EventHandler
    public void onNodeBreak(BlockBreakEvent e) {
        // Check if the block broken is:
        // a. within the world for a SkyNode
        // b. within the region for a SkyNode.
        // c. on the allowed-blocks list for a SkyNode.
        Messages messages = messagesManager.getMessages();
        Settings settings = settingsManager.getSettings();
        BukkitAudiences audiences = plugin.getAudiences();
        BlockVector3 blockVector3 = BlockVector3.at(e.getBlock().getLocation().getX(), e.getBlock().getLocation().getY(), e.getBlock().getLocation().getZ());
        List<SkyNode> allSkyNodes = skyNodeManager.getAllSkyNodes();

        for(SkyNode skyNode : allSkyNodes) {
            World world = skyNode.nodeWorld();
            ProtectedRegion region = skyNode.region();
            List<Material> materials = skyNode.materials();

            // World Check
            if(Objects.equals(e.getBlock().getWorld(), world)) {
                // Region Check
                if(region.contains(blockVector3)) {
                    if(e.getPlayer().hasPermission("skynodes.bypass.blockbreakcheck")) {
                        if(settings.debug() && e.getPlayer().hasPermission("skynodes.debug")) {
                            audiences.player(e.getPlayer()).sendMessage(messages.prefix().append(messages.bypassedBlockBreakCheck()));
                        }
                        return;
                    }

                    // Material Check
                    for(Material mat : materials) {
                        if(Objects.equals(e.getBlock().getType(), mat)) {
                            if(settings.debug() && e.getPlayer().hasPermission("skynodes.debug")) {
                                audiences.player(e.getPlayer()).sendMessage(messages.prefix().append(messages.canMine()));
                            }
                            return;
                        } else {
                            if(settings.debug() && e.getPlayer().hasPermission("skynodes.debug")) {
                                audiences.player(e.getPlayer()).sendMessage(messages.prefix().append(messages.canNotMine()));
                            }
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}
