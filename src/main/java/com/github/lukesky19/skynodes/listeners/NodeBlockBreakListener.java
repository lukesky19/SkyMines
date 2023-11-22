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

import com.github.lukesky19.skynodes.data.ConfigMessages;
import com.github.lukesky19.skynodes.data.ConfigSettings;
import com.github.lukesky19.skynodes.data.Node;
import com.github.lukesky19.skynodes.managers.ConfigManager;
import com.github.lukesky19.skynodes.managers.NodeManager;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class NodeBlockBreakListener implements Listener {
    static final MiniMessage mm = MiniMessage.miniMessage();
    @EventHandler
    public static void onNodeBreak(BlockBreakEvent e) {
        ConfigMessages configMessages = ConfigManager.getConfigMessages();
        ConfigSettings configSettings = ConfigManager.getConfigSettings();
        // Check if the block broken is:
        // a. within the defined region for a node.
        // b. on the allowed-blocks list for a node.
        Location location = e.getBlock().getLocation();
        ConfigSettings settings = ConfigManager.getConfigSettings();
        for (Node data : NodeManager.getNodeDataArrayList()) {
            if (Objects.equals(e.getBlock().getWorld(), data.nodeWorld())) {
                BlockVector3 blockVector3 = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                if (data.region().contains(blockVector3)) {
                    // Check if player has the bypass permission for block break protections.
                    if(e.getPlayer().hasPermission("skynodes.bypass.blockbreakcheck")) {
                        if(configSettings.debug()) {
                            e.getPlayer().sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.bypassedBlockBreakCheckMessage()));
                        }
                        return;
                    }

                    for(Material mat : data.materials()) {
                        if(!Objects.equals(e.getBlock().getType(), mat)) {
                            if (settings.debug()) {
                                e.getPlayer().sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.canMineMessage()));
                            }
                            return;
                        }
                    }

                    if (settings.debug()) {
                        e.getPlayer().sendMessage(mm.deserialize(configMessages.prefixMessage() + configMessages.canNotMineMessage()));
                    }
                    e.setCancelled(true);
                }
            }
        }
    }
}
