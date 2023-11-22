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
package com.github.lukesky19.skynodes;

import com.github.lukesky19.skynodes.commands.SkyNodeCommand;
import com.github.lukesky19.skynodes.data.ConfigMessages;
import com.github.lukesky19.skynodes.data.ConfigSettings;
import com.github.lukesky19.skynodes.data.Node;
import com.github.lukesky19.skynodes.listeners.NodeBlockBreakListener;
import com.github.lukesky19.skynodes.managers.ConfigManager;
import com.github.lukesky19.skynodes.managers.NodeManager;
import com.github.lukesky19.skynodes.managers.SchematicManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class SkyNodes extends JavaPlugin {
    static SkyNodes instance;
    static BukkitTask task;
    static final MiniMessage mm = MiniMessage.miniMessage();


    public static SkyNodes getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Store plugin main instance.
        instance = this;

        // (Re-)load config files.
        // (Re-)initializes data from config files.
        reloadConfigFiles();
        // Get plugin messages.
        ConfigMessages configMessages = ConfigManager.getConfigMessages();

        // Check if WorldEdit or FastAsyncWorldEdit is enabled.
        if (!Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldEdit")).isEnabled() || !Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit")).isEnabled()) {
            instance.getComponentLogger().error(mm.deserialize(configMessages.missingWorldEditorFastAsyncWorldEditMessage()));
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // (Re-)load the plugin.
        // (Re-)Initializes the plugin function(s), aka the BukkitTask/BukkitRunnable.
        try {
            reloadTasks();
            instance.getComponentLogger().info(mm.deserialize(configMessages.reloadMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set skynodes command executor and tabcompleter.
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setExecutor(new SkyNodeCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("skynodes")).setTabCompleter(new SkyNodeCommand());
        // Register blockBreakListener.
        Bukkit.getPluginManager().registerEvents(new NodeBlockBreakListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Cancel the BukkitTask on shutdown.
        if(task != null) {
            task.cancel();
        }
    }

    public static void reloadConfigFiles() {
        // Copy default config if needed.
        ConfigManager.copyDefaultConfig();

        // (Re-)load config
        try {
            ConfigManager.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // (Re-)load, (re-)build, and (re-)store configured node data.
        try {
            NodeManager.loadNodes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadTasks() {
        // Cancel previous BukkitTask if it exists.
        if (task != null) {
            task.cancel();
        }

        // Get plugin settings
        ConfigSettings configSettings = ConfigManager.getConfigSettings();
        // Get plugin messages
        ConfigMessages configMessages = ConfigManager.getConfigMessages();
        // Get stored node data.
        ArrayList<Node> nodeDataList = NodeManager.getNodeDataArrayList();
        if(nodeDataList.isEmpty()) {
            instance.getComponentLogger().warn(configMessages.noNodesFoundMessage());
            return;
        }

        // Create new BukkitRunnable that repeats for the configured time.
        // It selects a random node and picks a random schematic to paste for that node.
        task = new BukkitRunnable() {
            @Override
            public void run() {
                Node nodeData = nodeDataList.get(new Random().nextInt(nodeDataList.size()));
                try {
                    SchematicManager.pasteFromConfig(nodeData);
                    SkyNodes.instance.getComponentLogger().info(mm.deserialize(configMessages.consoleNodePasteSuccessMessage(), Placeholder.parsed("nodeid", nodeData.nodeId())));
                } catch (Exception e) {
                    SkyNodes.instance.getComponentLogger().info(mm.deserialize(configMessages.consoleNodePasteFailureMessage(), Placeholder.parsed("nodeid", nodeData.nodeId())));
                    throw new RuntimeException(e);
                }
            }
        }.runTaskTimer(instance, 1, 20L * configSettings.timeDelay());
    }
}
