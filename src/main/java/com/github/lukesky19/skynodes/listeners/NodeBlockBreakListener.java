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
import com.github.lukesky19.skynodes.records.Node;
import com.github.lukesky19.skynodes.records.Task;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.ArrayList;
import java.util.Objects;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class NodeBlockBreakListener implements Listener {
    final SkyNodes plugin;
    final ConfigManager cfgMgr;
    final NodeManager nodeMgr;
    final MessagesManager msgsMgr;
    final SettingsManager settingsMgr;
    final TaskManager taskMgr;
    Messages messages;
    Settings settings;
    public NodeBlockBreakListener(SkyNodes plugin) {
        this.plugin = plugin;
        cfgMgr = plugin.getCfgMgr();
        nodeMgr = plugin.getNodeMgr();
        msgsMgr = plugin.getMsgsMgr();
        settingsMgr = plugin.getSettingsMgr();
        taskMgr = plugin.getTaskMgr();
    }
    static final MiniMessage mm = MiniMessage.miniMessage();
    @EventHandler
    public void onNodeBreak(BlockBreakEvent e) {
        // Check if the block broken is:
        // a. within the defined region for a node.
        // b. on the allowed-blocks list for a node.
        Location location = e.getBlock().getLocation();
        messages = msgsMgr.getMessages();
        settings = settingsMgr.getSettings();

        ArrayList<Task> tasksList = taskMgr.getTasksList();
        for (Task task : tasksList) {
            for(Node node : task.nodeList()) {
                if (Objects.equals(e.getBlock().getWorld(), node.nodeWorld())) {
                    BlockVector3 blockVector3 = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                    if (node.region().contains(blockVector3)) {
                        // Check if player has the bypass permission for block break protections.
                        if(e.getPlayer().hasPermission("skynodes.bypass.blockbreakcheck")) {
                            if(settings.debug()) {
                                e.getPlayer().sendMessage(messages.prefixMessage().append(messages.bypassedBlockBreakCheckMessage()));
                            }
                            return;
                        }

                        for(Material mat : node.materials()) {
                            if(!Objects.equals(e.getBlock().getType(), mat)) {
                                if (settings.debug()) {
                                    e.getPlayer().sendMessage(messages.prefixMessage().append(messages.canMineMessage()));
                                }
                                return;
                            }
                        }

                        if (settings.debug()) {
                            e.getPlayer().sendMessage(messages.prefixMessage().append(messages.canNotMineMessage()));
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
